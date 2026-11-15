# Tabla de Equivalencias de Señales por Perfil de Fabricante (Iteración 6: Interoperabilidad)

## 1. Contexto Arquitectónico y Motivación

En el marco del diseño arquitectónico del sistema **SpotTrack** (Iteración 6 de ADD: *Sensor Interoperability*), se identificó como riesgo residual en la sección **4.3.6.7** que la heterogeneidad de sensores físicos de terceros presenta discrepancias en:
- Nombres y formatos de campo JSON (camelCase, snake_case, claves propietarias).
- Unidades de medida de tiempo (milisegundos vs. segundos).
- Semántica y taxonomía de los tipos de señal (`MOTION`, `PULSE`, `OCCUPIED`, `TRIGGER`).
- Crecimiento descontrolado de traductores en el adaptador de ingesta.

Para mitigar este riesgo residual y garantizar el cumplimiento de los escenarios de calidad **QA-06** (Interoperabilidad) y los concerns **CRN-13** y **CRN-08** (autonomía del dominio mediante Arquitectura Hexagonal y capas anticorrupción), este documento establece:
1. El **Catálogo Acotado de Perfiles de Fabricante Soportados**.
2. La **Tabla de Equivalencias de Campos hacia el Contrato Canónico v1**.
3. Las **Reglas de Normalización de Unidades y Tipos de Señal**.

---

## 2. Especificación del Contrato Canónico de Telemetría (v1)

Toda señal de sensor procesada por el adaptador de infraestructura (`SensorSignalListener`) debe ser traducida por un `SensorPayloadTranslator` al contrato canónico interno `TelemetrySignal` antes de transformarse en el comando de dominio `MotionSensorCaptureCommand`:

| Atributo Canónico | Tipo | Requerido | Valor por Defecto | Descripción |
|---|---|---|---|---|
| `deviceId` | `String` | Sí | — | Identificador único del sensor físico IoT emparejado con la máquina. |
| `signalType` | `String` | Sí | `"MOTION"` | Tipo de señal normalizada (`"MOTION"` para eventos de uso/movimiento). |
| `durationMs` | `long` | Sí | `0` | Duración del movimiento o pulso en milisegundos. |
| `timestamp` | `String` | No | Fecha/hora actual UTC | Marca temporal en formato estándar ISO-8601 UTC. |
| `schemaVersion` | `int` | Sí | `1` | Versión del contrato canónico (DDR-14). Permite evolucionar el esquema sin romper clientes. |

---

## 3. Catálogo Acotado de Perfiles de Fabricante (Bounded Profile Catalog)

Para evitar una proliferación arbitraria de traductores, el sistema acota el catálogo a los siguientes perfiles oficiales:

| Perfil ID (`payloadProfile`) | Fabricante / Origen | Protocolo Base | Formato Payload | Estado |
|---|---|---|---|---|
| `default` | SpotTrack Native Emulator / Dispositivo propio | MQTT (TLS 8883) | JSON (camelCase / snake_case) | **Activo** |
| `vendor-x` | Sensores Industriales PIR Genéricos (Third-Party) | MQTT (TLS 8883) | JSON (snake_case plano) | **Homologado** |
| `apex-motion` | Apex Fitness Equipment Telemetry | MQTT (TLS 8883) | JSON (anidado propietario) | **Homologado** |
| `fitpulse-edge` | FitPulse IoT Gateway | MQTT (TLS 8883) | JSON (claves compactas / codificadas) | **Homologado** |

---

## 4. Matriz de Equivalencias de Señales por Perfil

La siguiente matriz define el mapeo formal desde la estructura de datos externa de cada fabricante hacia los campos del contrato canónico v1:

| Atributo Canónico v1 | Perfil `default` (SpotTrack) | Perfil `vendor-x` (PIR Industrial) | Perfil `apex-motion` (Apex Fitness) | Perfil `fitpulse-edge` (FitPulse Gateway) |
|---|---|---|---|---|
| **`deviceId`** | `deviceId` o `device_id` | `sensor_id` | `telemetry_header.sensor_uid` | `dev_tag` |
| **`signalType`** | `signalType` o `signal_type` | `event` | `signal_code` | `st` |
| **`durationMs`** | `durationMs` o `duration_ms` *(ms)* | `duration_sec` *(segundos)* | `pulse_ms` *(milisegundos)* | `duration` *(segundos flotante)* |
| **`timestamp`** | `timestamp` *(ISO-8601)* | `ts_epoch` *(Epoch en segundos)* | `recorded_at` *(ISO-8601)* | `epoch_ms` *(Epoch en milisegundos)* |
| **`schemaVersion`** | `schemaVersion` o `schema_version` *(def: 1)* | *(No provisto, asume 1)* | `version` *(def: 1)* | `ver` *(def: 1)* |

---

## 5. Reglas de Transformación y Normalización

### 5.1. Normalización de Unidades de Tiempo (`durationMs`)

- **Perfil `default`:** La lectura se interpreta directamente en milisegundos. Si el campo está ausente, se normaliza a `0L`.
- **Perfil `vendor-x`:** La unidad de origen son segundos (`duration_sec`). La regla de transformación aplica:  
  $$\text{durationMs} = \text{round}(\text{duration\_sec} \times 1000)$$
- **Perfil `apex-motion`:** La lectura de `pulse_ms` es nativa en milisegundos y se asigna directamente.
- **Perfil `fitpulse-edge`:** Se recibe un valor decimal en segundos (`duration`). La regla de transformación aplica:  
  $$\text{durationMs} = \text{round}(\text{duration} \times 1000)$$

### 5.2. Taxonomía y Normalización de Tipos de Señal (`signalType`)

Cualquier nomenclatura heterogénea de evento que indique presencia o activación de la máquina de entrenamiento se mapea al tipo de señal unificado `"MOTION"` del dominio:

| Tipo Canónico Resultante | Valores Origen Aceptados en `default` | Valores Origen Aceptados en `vendor-x` | Valores Origen Aceptados en `apex-motion` | Valores Origen Aceptados en `fitpulse-edge` |
|---|---|---|---|---|
| **`"MOTION"`** | `"MOTION"`, `"motion"` | `"MOTION_DETECTED"`, `"OCCUPIED"`, `"TRIGGER"` | `"ACT_START"`, `"ACTIVE_PULSE"`, `"1"` | `"MOV_ON"`, `"IN_USE"`, `"TRUE"` |
| **`"HEARTBEAT"`** | `"HEARTBEAT"` | `"PING"`, `"KEEPALIVE"` | `"STATUS_OK"`, `"PING"` | `"BEAT"`, `"ALIVE"` |

### 5.3. Normalización de Marcas de Tiempo (`timestamp`)

- **Formatos ISO-8601 (`String`):** Se valida contra estándar `DateTimeFormatter.ISO_OFFSET_DATE_TIME` (o `ISO_INSTANT`).
- **Formatos Epoch Unix en Segundos (`long`/`double`):** Se transforma a fecha/hora UTC:  
  $$\text{Instant.ofEpochSecond(ts\_epoch).toString()}$$
- **Formatos Epoch Unix en Milisegundos (`long`):** Se transforma a fecha/hora UTC:  
  $$\text{Instant.ofEpochMilli(epoch\_ms).toString()}$$
- **Timestamp Ausente o Inválido:** Si el payload del sensor carece de timestamp, el traductor genera el timestamp actual del servidor (`Instant.now().toString()`), garantizando que la señal ingrese al dominio con marca de tiempo precisa.

---

## 6. Política de Lector Tolerante y Versionado (DDR-14)

1. **Lectura Tolerante:** Todos los traductores implementan la directiva `@JsonIgnoreProperties(ignoreUnknown = true)`. Campos auxiliares de fabricantes (tales como nivel de batería `battery`, temperatura ambiental `temp`, RSSI de red `rssi_dbm` o revisiones de firmware `firmware_rev`) son consumidos sin provocar errores de deserialización.
2. **Versionado de Esquema:** Todo payload recibido sin versión explícita se asume como versión `1` (compatibilidad hacia atrás). Ante un cambio de versión canónica futura (v2), los traductores podrán discriminar reglas de conversión por versión sin afectar a los sensores ya desplegados en el gimnasio.
3. **Resiliencia ante Payloads Inválidos:** Todo payload que no respete el formato de su perfil o contenga errores sintácticos devuelve un `Result.Failure(ApplicationError)` de tipo `VALIDATION_ERROR`, el cual es contabilizado en `RejectedSensorSignalRegistry` sin lanzar excepciones de runtime no controladas.


