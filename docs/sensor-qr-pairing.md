# Pairing de sensores por QR (TA-04)

## 1. Objetivo y alcance

Un administrador registra un sensor de movimiento y obtiene un **QR** que contiene todo lo que el dispositivo necesita para conectarse al broker MQTT y publicar, sin configuración manual.

Este documento describe el flujo completo. La implementación se entrega en dos PR:

| PR | Contenido |
|---|---|
| **PR 1** | Endpoint de emisión, agregado `SensorPairing`, QR, expiración, marca `USED` al primer heartbeat. El aprovisionamiento en los brokers es un **no-op** (`pairing.provisioner.mode=noop`): solo registra en el log, sin el secreto. |
| **PR 2** (este) | Adaptador real de aprovisionamiento en los 3 brokers (plugin `dynamic-security` de Mosquitto), script de arranque de los brokers y cambios en `infra/terraform`. Procedimiento de activación en [`infra/README.md`](../infra/README.md#activating-pairing-dynsec). |

> Con `pairing.provisioner.mode=noop` (el valor por defecto) el QR que se emite **no es utilizable contra los brokers reales**: la credencial no se crea en ellos. Para que lo sea hacen falta brokers con `mqtt_auth_mode=dynsec` **y** `pairing.provisioner.mode=dynsec`; la app falla al arrancar si se pide el segundo sin la configuración completa.

## 2. Diagrama de secuencia

```mermaid
sequenceDiagram
    autonumber
    actor Admin
    participant API as Backend (SpotTrack)
    participant DB as Base de datos
    participant B as Brokers Mosquitto (x3)
    participant Dev as Dispositivo (sensor)

    Admin->>API: POST /api/v1/monitoring/motion-sensors/{id}/pairing (JWT ADMIN)
    API->>DB: ¿existe el sensor?
    API->>API: genera secreto (SecureRandom, 24 bytes) y su hash
    API->>B: provision(deviceId, secreto) — todo o nada en los 3 brokers
    alt aprovisionamiento falla en algún broker
        B-->>API: error
        API-->>Admin: 500 UNEXPECTED_ERROR (nada queda persistido)
    else aprovisionamiento correcto
        B-->>API: ok
        API->>DB: pairing anterior del sensor → REVOKED
        API->>DB: nuevo SensorPairing PENDING (secretHash, expiresAt = ahora + TTL)
        API-->>Admin: 200 {payload, payloadJson, qrPngBase64} (Cache-Control: no-store)
    end

    Admin->>Dev: muestra el QR; el dispositivo lo escanea
    Dev->>Dev: valida el JSON (v, campos, exp)
    Dev->>B: CONNECT ssl://host:8883, usuario=deviceId, clave=secret, CA verificada con caSha256
    Dev->>B: PUBLISH sensors/{deviceId}/heartbeat
    B-->>API: heartbeat (suscripción del backend)
    API->>DB: SensorPairing PENDING → USED (usedAt)
    Note over API,B: La credencial se queda tras USED. Solo se revoca al regenerar el pairing.

    opt el QR caduca sin que llegue ningún heartbeat
        API->>API: proceso programado: PENDING vencido
        API->>B: revoke(deviceId)
        API->>DB: SensorPairing → EXPIRED
    end
```

## 3. Formato del QR v1

El QR contiene un JSON compacto UTF-8:

```json
{"v":1,"deviceId":"066dd222-31ed-496a-bddf-e4711b0ee957","secret":"Zk3Qm...32 caracteres","host":"34.123.45.67","port":8883,"caSha256":"9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08","exp":1790000000}
```

| Campo | Tipo | Descripción |
|---|---|---|
| `v` | int | Versión del formato. Esta es la `1`. Un dispositivo debe rechazar versiones que no conozca. |
| `deviceId` | string | `motionSensorId` (UUID). Es el **usuario MQTT** y parte de los topics. |
| `secret` | string | Contraseña MQTT: 24 bytes aleatorios en Base64 URL sin relleno (32 caracteres). |
| `host` | string | Host del balanceador de brokers (`mqtt.public-endpoint`). |
| `port` | int | Puerto TLS, `8883`. |
| `caSha256` | string | SHA-256 en hexadecimal minúsculas del certificado de la CA, en DER. |
| `exp` | int | Epoch en segundos (UTC) hasta el que el QR es válido **si no se ha escaneado**. |

La CA **no va en el QR**: el dispositivo debe tenerla (firmware o descarga) y comprobar que su huella coincide con `caSha256` antes de confiar en ella. Para obtener la huella:

```bash
openssl x509 -in mqtt-ca.crt -outform DER | openssl dgst -sha256 -hex
```

Topics, derivados del `deviceId` (el ACL del broker solo permite escribir en los propios):

- `sensors/{deviceId}/signal`
- `sensors/{deviceId}/heartbeat`

El formato del payload de `signal` depende del `payloadProfile` del sensor (ver `interoperability-signal-equivalences.md`).

## 4. Endpoint de emisión

`POST /api/v1/monitoring/motion-sensors/{motionSensorId}/pairing`, solo rol **ADMIN**.

Respuesta `200`:

```json
{
  "motionSensorId": "066dd222-...",
  "expiresAt": "2026-11-15T12:15:00",
  "payload": { "v": 1, "deviceId": "...", "secret": "...", "host": "...", "port": 8883, "caSha256": "...", "exp": 1790000000 },
  "payloadJson": "{\"v\":1,...}",
  "qrPngBase64": "iVBORw0KGgo..."
}
```

`payloadJson` es exactamente el texto codificado en el QR (JSON compacto, campos en el orden de la sección 3) y `qrPngBase64` su imagen PNG de 320×320 px (corrección de errores M), en Base64 estándar.

La respuesta, también la de error, lleva `Cache-Control: no-store`. El secreto se devuelve **una sola vez**: en la base de datos solo se guarda su hash BCrypt y no se escribe en ningún log.

| Caso | Respuesta |
|---|---|
| Sin token | `401` |
| Token sin rol ADMIN | `403` |
| Sensor inexistente | `404 MOTIONSENSOR_NOT_FOUND` |
| `motionSensorId` en blanco | `400 VALIDATION_ERROR` |
| `mqtt.public-endpoint` o `mqtt.ca-sha256` sin configurar o mal formados (ver sección 5) | `422 BUSINESS_RULE_VIOLATION`; `details` indica la propiedad (p. ej. `mqtt.public-endpoint is not configured (expected host:port)`). No se toca ningún broker ni se persiste nada |
| Falla el aprovisionamiento en los brokers | `500 UNEXPECTED_ERROR`; no se persiste nada |
| Falla la base de datos después de aprovisionar | `500 UNEXPECTED_ERROR`; se intenta un `revoke` del dispositivo (ver riesgo 10) |

El sensor se comprueba antes que la configuración: un sensor inexistente responde `404` aunque falte configuración.

## 5. Configuración

| Propiedad | Variable de entorno | Por defecto | Descripción |
|---|---|---|---|
| `pairing.ttl` | `PAIRING_TTL` | `15m` | Validez de un QR **sin escanear** (formato de duración de Spring: `15m`, `PT15M`). Debe ser positiva. |
| `pairing.provisioner.mode` | `PAIRING_PROVISIONER_MODE` | `noop` | `noop`: no toca los brokers, solo registra el `deviceId`. `dynsec`: crea/borra la cuenta del sensor en **cada** broker con el plugin `dynamic-security` (ver 5.1). Con `dynsec` la aplicación **no arranca** si falta algún dato (ver 5.1). |
| `pairing.expiry-check-interval-ms` | `PAIRING_EXPIRY_CHECK_INTERVAL_MS` | `60000` | Cada cuánto el proceso programado busca pairing vencidos; la expiración real puede retrasarse hasta ese intervalo. |
| `mqtt.public-endpoint` | `MQTT_PUBLIC_ENDPOINT` | `localhost:8883` (dev) / vacío (prod) | `host:puerto` TLS del balanceador visto por los dispositivos. Puerto entre 1 y 65535. |
| `mqtt.ca-sha256` | `MQTT_CA_SHA256` | 64 ceros (dev) / vacío (prod) | SHA-256 de la CA en hexadecimal; se aceptan mayúsculas y los `:` de `openssl`, y se normaliza a minúsculas sin `:`. Debe tener 64 caracteres hexadecimales. |

En `prod` los dos últimos no tienen valor por defecto: la aplicación arranca, pero la emisión responde `422` hasta que se configuren. El valor de dev (ceros) no corresponde a ninguna CA real, así que un QR emitido con él no pasa la comprobación de `caSha256` del dispositivo.

Si `mqtt.ca-sha256` está vacío y `mqtt.ca-cert` (PEM o base64 de un PEM) es válido, la aplicación **deriva** el SHA-256 del certificado DER de la CA. En `prod` basta con `MQTT_CA_CERT`; Terraform ya lo pasa. Un certificado inválido solo deja el valor vacío (aviso en el log) salvo con `mode=dynsec`, donde es un error de arranque.

### 5.1 Aprovisionador `dynsec`

| Propiedad | Variable de entorno | Por defecto | Descripción |
|---|---|---|---|
| `mqtt.dynsec.admin-username` | `MQTT_DYNSEC_ADMIN_USERNAME` | `spottrack-dynsec-admin` | Cuenta admin del plugin, **distinta** de `mqtt.username`. Solo puede publicar/leer en `$CONTROL/dynamic-security/#`. |
| `mqtt.dynsec.admin-password` | `MQTT_DYNSEC_ADMIN_PASSWORD` | vacío | Secreto `mqtt-dynsec-admin-password` de Secret Manager (lo escribe `deploy.sh`). Nunca se registra en logs. |
| `pairing.dynsec.sensor-role` | `PAIRING_DYNSEC_SENSOR_ROLE` | `sensor` | Rol que se asigna a cada dispositivo. |
| `pairing.dynsec.connect-timeout` / `response-timeout` | `PAIRING_DYNSEC_CONNECT_TIMEOUT` / `PAIRING_DYNSEC_RESPONSE_TIMEOUT` | `3s` / `3s` | Conexión TLS corta por broker y espera de la respuesta. |
| `pairing.dynsec.max-attempts` | `PAIRING_DYNSEC_MAX_ATTEMPTS` | `2` | Intentos por broker, **solo** ante errores de transporte (no ante una respuesta de error del broker). |
| `pairing.dynsec.total-timeout` | `PAIRING_DYNSEC_TOTAL_TIMEOUT` | `15s` | Plazo total de una operación contra los 3 brokers, reintentos incluidos. |

Con `mode=dynsec` la aplicación falla al iniciar, con un mensaje que lista todos los problemas, si: `mqtt.broker-urls` está vacío o alguna URL no es `ssl://`; falta el usuario o la contraseña del admin; `mqtt.ca-cert` falta o no es un certificado válido; el rol está vacío; `max-attempts` < 1 o algún plazo no es positivo.

### 5.2 Semántica del aprovisionador

- **Mensaje único por broker.** `deleteClient` + `createClient` + `addClientRole` viajan en un solo mensaje de control (`$CONTROL/dynamic-security/v1`), a los 3 brokers en paralelo. El conjunto es idempotente (un `deleteClient` de un cliente inexistente responde «Client not found» y no aborta los siguientes), por eso se puede reintentar tras un error de transporte.
- **`provision` es todo-o-nada.** Si falla en algún broker se hace `deleteClient` en **todos** (también en el que falló, que pudo aplicar el cambio antes de agotar el plazo) y se lanza `BrokerProvisioningException`. Como `provision` sustituye una credencial anterior, un reemisión fallida deja al dispositivo **sin credencial** en los brokers (falla cerrado), no con la vieja a medias.
- **`revoke` es idempotente y de mejor esfuerzo.** Tolera «Client not found», intenta **todos** los brokers aunque alguno falle y lanza la excepción al final si alguno falló (el proceso programado lo reintenta). Es la única desviación del todo-o-nada: no hay forma de «des-revocar».
- **`device_id`**: `[A-Za-z0-9._-]{1,64}`; se rechazan `spottrack-backend` y `spottrack-dynsec-admin`. Los comodines `+`/`#` los acepta `createClient` pero se prohíben aquí por defensa en profundidad.
- **El secreto no aparece** en logs, `toString` ni mensajes de excepción (hay tests).

### 5.3 Modelo del broker en modo `dynsec`

`broker-startup.sh` crea los roles y deja el plugin con `default deny` en los cuatro tipos de ACL:

| Rol | ACL (nombres de Mosquitto 2.1.2) |
|---|---|
| `sensor` | `publishClientSend sensors/%u/signal` y `sensors/%u/heartbeat`. Sin permiso de suscripción. `%u` se sustituye en las ACL de rol. |
| `backend-reader` | `subscribeLiteral` + `publishClientReceive` sobre `sensors/+/signal` y `sensors/+/heartbeat`. |
| `admin` (el que crea `dynsec init`, **reducido**) | `publishClientSend`, `publishClientReceive` y `subscribePattern` solo sobre `$CONTROL/dynamic-security/#`. Por defecto `init` le da lectura de `#` y `$SYS/#`; el script lo quita. |

Cuentas: `spottrack-backend` (rol `backend-reader`), `spottrack-dynsec-admin` (rol `admin`), los sensores de `mqtt_device_ids` (rol `sensor`, **solo si no existen**: no se pisa la contraseña de uno ya emparejado) y las cuentas que cree el backend. Un sensor revocado de `mqtt_device_ids` se **recrea** en el siguiente arranque del broker: quítelo de `mqtt_device_ids` cuando ya esté emparejado.

Hallazgos verificados con `eclipse-mosquitto:2.1.2-alpine`: `deleteClient` y `setClientPassword` desconectan al cliente conectado (`disconnected: administrative action`) y sus reconexiones reciben CONNACK 135; con un `device_id` con comodín `+`, `createClient` lo acepta pero publicar a otro dispositivo da RC 135; todas las respuestas del plugin llegan a **todos** los suscriptores de `.../v1/response`, por eso cada comando lleva `correlationData`.

## 6. Ciclo de vida del pairing

| Estado | Significado |
|---|---|
| `PENDING` | Emitido y aún sin escanear. Caduca en `pairing.ttl` (por defecto 15 min). |
| `USED` | Llegó el primer heartbeat (o señal de movimiento) del sensor, con el pairing aún vigente. La credencial **se queda**; el TTL deja de aplicar. |
| `EXPIRED` | Venció sin uso. El proceso programado pidió quitar la credencial a los brokers. |
| `REVOKED` | Reemplazado por un pairing nuevo del mismo sensor. |

Regenerar el pairing de un sensor invalida el secreto anterior: se aprovisiona el secreto nuevo (que sustituye al previo en los brokers) y los pairing `PENDING` o `USED` anteriores pasan a `REVOKED`.

El proceso programado solo toca pairing `PENDING` vencidos: un pairing `USED` no se revoca ni pasa a `EXPIRED` por mucho que haya pasado el TTL. Primero pide el `revoke` a los brokers y, si tiene éxito, lo marca `EXPIRED`.

Si al caducar el `revoke` falla, el pairing se queda `PENDING` y se reintenta en la siguiente pasada del proceso programado (otros pairing vencidos no se bloquean). Un heartbeat que llega con el pairing ya vencido **no** lo marca `USED`.

## 7. QR inválido, expirado o ya usado

El backend **no participa en el escaneo**: solo emite el QR y observa el primer heartbeat. Por eso estos casos se detectan en el dispositivo o los rechaza el broker.

| Situación | Quién lo detecta | Qué debe hacer el dispositivo |
|---|---|---|
| JSON mal formado, campos ausentes o `v` desconocida | Dispositivo | No conectar. Mostrar «QR no válido» y volver al escáner. |
| `caSha256` no coincide con la CA que tiene | Dispositivo | No conectar ni enviar credenciales. Mostrar «CA no reconocida». |
| `exp` ya pasó según su reloj | Dispositivo | No conectar. Mostrar «QR caducado, pida uno nuevo». |
| `exp` vigente pero el pairing ya caducó o fue reemplazado en el backend | Broker | El broker rechaza el `CONNECT` (MQTT 3.1.1: código 5, no autorizado; MQTT 5: `0x86`/`0x87`). Reintentar con backoff, máximo unos pocos intentos, y luego mostrar «credenciales rechazadas, pida un QR nuevo». Nunca reintentar en bucle. |
| QR ya escaneado (`USED`) | Nadie | La credencial sigue siendo válida hasta que se regenere el pairing, así que el broker **no** distingue un segundo dispositivo. Ver riesgos. |

Tras un pairing correcto, el dispositivo debe guardar las credenciales en almacenamiento seguro y descartar el QR escaneado.

## 8. Riesgos conocidos

1. **`permitAll` en `/api/v1/monitoring/motion-sensors/**`.** `WebSecurityConfiguration` deja ese patrón sin autenticación, por lo que hoy cualquiera puede registrar sensores (`POST`) y listarlos. Los endpoints de pairing quedan excluidos de ese `permitAll` mediante una regla `hasRole('ADMIN')` sobre `/api/v1/monitoring/motion-sensors/*/pairing/**`, colocada antes del `permitAll`, y además llevan `@PreAuthorize`; el resto del patrón sigue abierto. Cerrarlo se deja para otro cambio porque puede romper al simulador IoT.
2. **`GET /api/v1/monitoring/motion-sensors/devices` publica los `device_id`**, que son los usuarios MQTT. Conocer el usuario es la mitad de la credencial.
3. **Una credencial `USED` no caduca.** Un QR filtrado tras el primer escaneo sigue siendo un secreto válido hasta que un administrador regenere el pairing. El TTL solo limita la ventana *antes* del primer uso.
4. **Cualquier ADMIN puede emitir el pairing de cualquier sensor.** No se comprueba que el equipo pertenezca a su gimnasio, de modo que un administrador puede rotar la credencial de un sensor ajeno y dejarlo sin conexión.
5. **El QR es un secreto en pantalla.** Puede quedar en capturas o fotos. Hay que mostrarlo solo a quien vaya a escanearlo, y regenerarlo si se sospecha de filtración.
6. **Desfase de reloj.** `exp` se calcula con el reloj del servidor; un dispositivo con la hora muy desajustada podría rechazar un QR válido o aceptar uno vencido (el broker lo rechazará de todos modos).
7. **Brokers independientes sin bridging.** El balanceador reparte a cualquiera de los 3, así que una credencial presente solo en algunos provoca fallos intermitentes. De ahí la semántica todo-o-nada del puerto `BrokerCredentialProvisioner`. El estado del plugin vive en `/var/lib/mosquitto-dynsec` de cada VM: sobrevive a un `reset`/reinicio, pero **se pierde si se recrea la VM** (`taint`, cambio de disco). Esta entrega no reaprovisiona automáticamente a los sensores emparejados tras recrear un broker: hay que regenerar sus pairing (queda una cuenta ausente en ese broker y los dispositivos que caigan en él serán rechazados).
8. **El hash BCrypt no se verifica en el backend.** Quien autentica es el broker; el hash sirve como registro de qué secreto se emitió y para una futura verificación.
9. **Sin límite de intentos** en el endpoint de emisión.
10. **Un fallo de base de datos tras aprovisionar deshace también la credencial anterior.** Si `provision` tiene éxito y el guardado falla, se hace un `revoke` del dispositivo para no dejar en los brokers un secreto sin registro; como el `provision` ya había sustituido el secreto anterior, el sensor se queda sin credencial hasta que se emita otro pairing.
11. **Un QR vigente se pierde si el heartbeat llega tarde.** Un heartbeat posterior al TTL no marca `USED`, y el proceso programado revoca la credencial en la siguiente pasada; el dispositivo debe pedir un QR nuevo.
12. **`mosquitto_ctrl` no es fiable con su código de salida.** Un fallo TLS sale con `0` y sin salida; los errores lógicos («Client already exists», «ACL not found»…) también salen con `0`; `addClientRole` sobre un rol ya asignado responde `Error: Internal error.`. El script de arranque, por eso, inspecciona la salida, verifica el estado final (`listClients`, `getRole`, `getDefaultACLAccess`) y **nunca** usa `$?`; si la siembra falla sale con código distinto de 0 y deja el broker parado.
13. **Contraseñas en el `argv` durante la siembra.** `createClient <user> -p <password>` se ejecuta en un contenedor efímero; la contraseña es visible un instante en la lista de procesos de la VM del broker (que solo administra el equipo).
14. **Hallazgo en el modo estático.** Con `password_file` + `acl_file`, el broker **concede** a un sensor la suscripción a `#` y a `sensors/#` (SUBACK 0, verificado en 2.0.22 y 2.1.2). En la prueba con un segundo sensor publicando, el suscriptor **no recibió** el mensaje (el ACL se aplica también al entregar), así que no hay fuga de datos observada, pero sí una suscripción concedida que no debería existir y que mantiene recursos del broker. En modo `dynsec` el sensor no tiene permiso de suscripción: SUBACK 135.
