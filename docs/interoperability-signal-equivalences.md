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


