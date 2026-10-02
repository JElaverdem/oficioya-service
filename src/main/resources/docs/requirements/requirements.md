# 📄 Requerimientos del Sistema

## 1. Lista general de requerimientos

El sistema de service OficioYa tiene los siguientes requerimientos (descripción a alto nivel): Debe permitir

### 1.1 Requerimientos funcionales

El sistema de service OficioYa debe tener la capacidad de:

#### 1. Creacion de Solicitudes
1. El sistema debe permitir al contratante crear una solicitud de servicio indicando descripcion en texto, fotografia, zona (barrio y direccion exacta), fecha y hora.
2. El sistema le debe permitir al contratante escoger los trabajadores a enviar la solicitud.
3. El sistema debe permitir al contratante enviar la misma solicitud a un solo trabajador o a multiples trabajadores de la zona de forma simultanea. 

#### 2. Gestion y Estados de la Solicitud
<!--//4. El sistema debe permitir al trabajador aceptar o rechazar una solicitud entrante en un plazo maximo de 30 minutos.-----
//5. Si el trabajador no responde en 30 minutos, el sistema debe cambiar automaticamente el estado de la solicitud a Expirada.
6. Si multiples trabajadores aceptan una solicitud enviada en grupo, el sistema debe solicitar al contratante que cancele las que no desea tomar.-->
7. El sistema le debe permitir a un trabajador completar un trabajo.

#### 3. Cancelaciones y Penalidades
8. El sistema debe permitir al contratante cancelar una solicitud en cualquier momento si esta aún no ha sido aceptada.
9. El sistema debe permitir a ambas partes (contratante y trabajador) cancelar una solicitud aceptada hasta 24 horas antes de la hora pactada sin penalizacion.
10. Si el contratante cancela con menos de 24 horas de anticipacion, el sistema debe bloquearlo para realizar nuevas solicitudes a ese mismo trabajador durante una semana.
11. Si el trabajador cancela con menos de 24 horas de anticipacion, el sistema debe penalizar su reputacion restando 0,5 puntos.

#### 4. Consultas
12. El sistema debe permitir al contratante y al trabajador consultar el listado de sus solicitudes activas e historicas.

### 1.2 Requerimientos no funcionales

El sistema de service OficioYa debe tener:

1. El sistema debe gestionar las solicitudes bajo los siguientes estados exactos: Pendiente, Aceptada, Conflicto, Cancelada, Rechazada, Cumplida, Expirada y Eliminada.
2. Las solicitudes eliminadas no deben borrarse fisicamente de la base de datos (eliminacion logica).
3. Si una solicitud se envia a multiples trabajadores, el sistema debe crearla como solicitudes independientes para cada trabajador.
4. Al revisar el historial de solicitudes Aparecen con el nombre del contratante si es una solicitud a un trabajador o el nombre del trabajador si es una solicitud que hizo el contratante, el día y hora a la que se va a realizar o se realizó el trabajo y el estado en el que está.
5. Al revisar el historial de solicitudes no aparecen las solicitudes eliminadas lógicamente.

## 2. Diagramas de caso de uso

### 2.1 Requerimiento Funcional 1

| Campo | Descripción |
|------|-------------|
| **ID** | REQ-SRV-001 |
| **Nombre del requerimiento** | Creación de una solicitud |
| **Descripción** | El sistema debe permitir al contratante crear una solicitud de servicio indicando descripcion en texto, fotografia, zona (barrio y direccion exacta), fecha y hora, dejándola lista para ser enviada a un trabajador o más. |
| **Precondiciones** | Para que el sistema cumpla con este requerimiento, el contratante ya debe de haber iniciado sesión. |
| **Actor** | Contratante |
| **Flujo principal** | 1. El contratante escoge crear una nueva solicitud.<br>2. El contratante agrega la descripción del servicio que se quiere hacer.<br>3. El contratante agrega opcionalmente una o más fotografías del trabajo a hacer.<br>4. El usuario escoge el barrio donde se encuentra y la dirección.<br>5. EL usuario agrega la fecha y la hora en la que se va a realizar el trabajo.<br>6. El usuario termina y confirma los datos de la solicitud.<br>7. El sistema verifica que todos los datos de la solicitud sean correctos. |
| **Diagrama de caso de uso** | ![Diagrama caso uso - 1](../images/DCU-REQ-SRV-001) |
| **Poscondiciones** | Se espera como resultado la solicitud lista para enviar con los datos verificados. |

### 2.2 Requerimiento Funcional 2

| Campo | Descripción |
|------|-------------|
| **ID** | REQ-SRV-003 |
| **Nombre del requerimiento** | Selección de trabajadores|
| **Descripción** | El sistema le debe permitir al contratante escoger los trabajadores a enviar la solicitud. |
| **Precondiciones** | Para que el sistema cumpla con este requerimiento, la solicitud ya debe de estar creada y verificada, los datos del barrio son correctos. |
| **Actor** | Contratante y trabajadores |
| **Flujo principal** | 1. El contratante termina de llenar la solicitud.<br>2. El sistema verifica los datos de la solicitud.<br>3. El sistema lleva al contratante a escoger los trabajadores.<br>4. El sistema le muestra los trabajadores disponibles para esa zona.<br>5. El contratante escoge uno o varios trabajadores.<br>6. El contratante confirma los trabajadores conocidos. |
| **Diagrama de caso de uso** | ![Diagrama caso uso - 2](../images/DCU-REQ-SRV-002) |
| **Poscondiciones** | Se espera como resultado los trabajadores que el contratante escogió verificados y las personas listas para enviar las solicitudes. |

### 2.3 Requerimiento Funcional 3

| Campo | Descripción |
|------|-------------|
| **ID** | REQ-SRV-003 |
| **Nombre del requerimiento** | Envío de solicitudes |
| **Descripción** | El sistema debe permitir al contratante enviar la misma solicitud a un solo trabajador o a multiples trabajadores de forma simultanea, al terminar y enviar la/s solicitud/es, se les debe enviar una notificación a los trabajadores, para esto, se conecta a la API de notificaciones. |
| **Precondiciones** | Para que el sistema cumpla con este requerimiento, la solicitud ya debe de estar completada y verificada, el usuario ya escogió los trabajadores y el sistema ya verificó estos trabajadores. |
| **Actor** | Contratante y trabajadores |
| **Flujo principal** | 1. El contratante termina de escoger los trabajadores.<br>2. El contratante confirma que quiere enviar la/s solicitud/es<br>3. El sistema envía las solicitudes a los trabajadores.<br>4. El sistema envía una notificación a los trabajadores. |
| **Diagrama de caso de uso** | ![Diagrama caso uso - 3](../images/DCU-REQ-SRV-003) |
| **Poscondiciones** | Se espera como resultado que las solicitudes por separado se hayan creado y enviado a cada trabajador, y que a cada uno le llegue una notificación de una nueva solicitud. |
<!--
### 2.4 Requerimiento Funcional 4

| Campo | Descripción |
|------|-------------|
| **ID** | REQ-SRV-004 |
| **Nombre del requerimiento** | |
| **Descripción** | *El sistema debe …* |
| **Precondiciones** | *Para que el sistema cumpla con este requerimiento, Bankify debe tener previamente …* |
| **Actor** | *(El actor debe estar definido en el diagrama de contexto)* |
| **Flujo principal** | 1. El actor …<br>2. El sistema …<br>3. El sistema … |
| **Diagrama de caso de uso** | *imagen y link*|
| **Poscondiciones** | *Se espera como resultado …* |

### 2.5 Requerimiento Funcional 5

| Campo | Descripción |
|------|-------------|
| **ID** | REQ-SRV-005 |
| **Nombre del requerimiento** | |
| **Descripción** | Si el trabajador no responde en 30 minutos, el sistema debe cambiar automaticamente el estado de la solicitud a Expirada. |
| **Precondiciones** | *Para que el sistema cumpla con este requerimiento, Bankify debe tener previamente …* |
| **Actor** | *(El actor debe estar definido en el diagrama de contexto)* |
| **Flujo principal** | 1. El actor …<br>2. El sistema …<br>3. El sistema … |
| **Diagrama de caso de uso** | *imagen y link*|
| **Poscondiciones** | *Se espera como resultado …* |
-->
### 2.6 Requerimiento Funcional 6

| Campo | Descripción |
|------|-------------|
| **ID** | REQ-SRV-006 |
| **Nombre del requerimiento** | Manejo de varias solicitudes aceptadas |
| **Descripción** | Si multiples trabajadores aceptan una solicitud enviada en grupo, el sistema debe solicitar al contratante que cancele las que no desea tomar. |
| **Precondiciones** | Para que el sistema cumpla con este requerimiento, las solicitudes a los demás trabajadores ha sido enviada, más de un trabajador aceptó la solicitud antes de que se expirara. |
| **Actor** | Contratante y trabajadores |
| **Flujo principal** | 1. El contratante envía una solicitud a más de un trabajador.<br>2. Más de un trabajador acepta la solicitud antes de que expirara.<br>3. El sistema cambia la solicitud a estado Conflicto.<br>4. El sistema le envía una notificación al contratante de que la solicitud fue aceptada por más de un trabajador.<br>5. El contratante entra a mirar la solicitud.<br>6. El sistema le muestra al contratante los trabajadores que aceptaron la solicitud.<br>7. El contrante escoge un trabajador para que realice el trabajo.<br>8. El sistema cancela las solicitudes de los contratantes no escogidos y la de los que aún no han aceptado la solicitud. |
| **Diagrama de caso de uso** | ![Diagrama caso uso - 6](../images/DCU-REQ-SRV-006) |
| **Poscondiciones** | Se espera como resultado que se registre bien la solicitud al trabajador escogido y que ponga en estado 'Cancelada' la solicitud de los trabajadores no escogidos y que aún no han aceptado o rechazado la solicitud. |

### 2.7 Requerimiento Funcional 7

| Campo | Descripción |
|------|-------------|
| **ID** | REQ-SRV-007 |
| **Nombre del requerimiento** | Completar un trabajo |
| **Descripción** | El sistema le debe permitir a un trabajador completar un trabajo y enviar una notificación conectandose con la API de notifications para avisar al contratante que el trabajo fue finalizado. |
| **Precondiciones** | Para que el sistema cumpla con este requerimiento, el trabajo ya debe estarse haciendo, la hora debe ser después de la hora del trabajo. |
| **Actor** | Contratante y trabajador |
| **Flujo principal** | 1. El trabajador termina el trabajo.<br>2. El trabajador entra al sistema para terminar el trabajo.<br>3. El trabajador escoge terminar el trabajo.<br>4. El sistema hace las verificaciones para poder terminar el trabajo.<br>5. El sistema marca el trabajo como cumplida.<br>6. El sistema se conecta con notificaciones y le envía una notificación al contratante. |
| **Diagrama de caso de uso** | ![Diagrama caso uso - 7](../images/DCU-REQ-SRV-007) |
| **Poscondiciones** | Se espera como resultado el trabajo marcado como cumplida y la notificación enviada al contratante. |

### 2.8 Requerimiento Funcional 8

| Campo | Descripción |
|------|-------------|
| **ID** | REQ-SRV-008 |
| **Nombre del requerimiento** | Cancelar solicitud sin aceptar |
| **Descripción** | El sistema debe permitir al contratante cancelar una solicitud en cualquier momento si esta aún no ha sido aceptada. |
| **Precondiciones** | Para que el sistema cumpla con este requerimiento, el contratante ya debió de haber enviado una solicitud a un trabajador, el trabajador aún no ha aceptado la solicitud que se le ha enviado. |
| **Actor** | Contratante |
| **Flujo principal** | 1. El contratante envía una solicitud al trabajador.<br>2. El contratante ve que no ha sido aceptada y decide cancelarla.<br>3. El sistema cambia el estado de la solicitud a cancelada.<br>4. El sistema le envía una notificación al trabajador que la solicitud fue cancelada. |
| **Diagrama de caso de uso** | ![Diagrama caso uso - 8](../images/DCU-REQ-SRV-008) |
| **Poscondiciones** | Se espera como resultado que la solicitud quede en estado cancelada y que se le haya enviado la notificación al trabajador. |

### 2.9 Requerimiento Funcional 9

| Campo | Descripción |
|------|-------------|
| **ID** | REQ-SRV-009 |
| **Nombre del requerimiento** | Cancelar solicitud sin penalización |
| **Descripción** | El sistema debe permitir a ambas partes (contratante y trabajador) cancelar una solicitud aceptada hasta 24 horas antes de la hora pactada sin penalizacion. |
| **Precondiciones** | Para que el sistema cumpla con este requerimiento, la solicitud que se quiere cancelar ya debe de estar aceptada, quedan más de 24 horas antes de la fecha del trabajo. |
| **Actor** | Contratante y trabajador |
| **Flujo principal** | **Primer flujo**<br>1. El contratante revisa los trabajos abiertos.<br>2. El contratante escoge un trabajo el cual se realiza dentro de más de 24 horas.<br>3. El contratante elige cancelar la solicitud.<br>4. El sistema cambia el estado de la solicitud a cancelada.<br>5. El sistema envía una notificación al trabajador de que la solicitud fue cancelada.<br>**Segundo flujo**<br>1. El trabajador revisa los trabajos que tiene pendientes.<br>2. El trabajador escoge un trabajo el cual se realiza dentro de más de 24 horas.<br>3. El trabajador escoge cancelar este trabajo.<br>4. El sistema cambia el estado de la solicitud a cancelada.<br>5. El sistema le envía una notificación al contratante de que el trabajo fue cancelado. |
| **Diagrama de caso de uso** | ![Diagrama caso uso - 9](../images/DCU-REQ-SRV-009) |
| **Poscondiciones** | Se espera como resultado la solicitud en estado cancelada y la notificación enviada al contratante o trabajador. |

### 2.10 Requerimiento Funcional 10

| Campo | Descripción |
|------|-------------|
| **ID** | REQ-SRV-0010 |
| **Nombre del requerimiento** | Contratante cancela con menos 24 horas de anticipación |
| **Descripción** | Si el contratante cancela con menos de 24 horas de anticipacion, el sistema debe bloquearlo para realizar nuevas solicitudes a ese mismo trabajador durante una semana. |
| **Precondiciones** | Para que el sistema cumpla con este requerimiento, la solicitud ya debe haber sido aceptada, faltan menos de 24 horas para que se realice el trabajo. |
| **Actor** | Contratante y trabajador |
| **Flujo principal** | 1. El contratante revisa los trabajos abiertos.<br>2. El contratante escoge un trabajo que se realiza en menos de 24 horas.<br>3. El contrantante cancela el trabajo.<br>4. El sistema verifica si faltan menos de 24 horas para el trabajo.<br>5. El sistema comprueba que faltan menos de 24 horas para el trabajo.<br>6. El sistema bloquea al contrantante de poder enviar una solicitud al trabajador que le canceló el trabajo.<br>7. El sistema le envía una notificación al trabajador de que el trabajo fue cancelado. |
| **Diagrama de caso de uso** | ![Diagrama caso uso - 10](../images/DCU-REQ-SRV-0010) |
| **Poscondiciones** | Se espera como resultado el bloqueo del contratante para enviarle una solicitud de nuevo al mismo trabajador dentro de una semana, la solicitud en estado cancelada y la notificación enviada al trabajador. |

### 2.11 Requerimiento Funcional 11

| Campo | Descripción |
|------|-------------|
| **ID** | REQ-SRV-0011 |
| **Nombre del requerimiento** | Trabajador cancela con menos de 24 horas de anticipación|
| **Descripción** | Si el trabajador cancela con menos de 24 horas de anticipacion, el sistema debe penalizar su reputacion restando 0,5 puntos. |
| **Precondiciones** | Para que el sistema cumpla con este requerimiento, la solicitud ya debe haber sido aceptada, faltan menos de 24 horas para que se realice el trabajo. |
| **Actor** | Contratante y trabajador |
| **Flujo principal** | 1. El trabajador revisa los trabajos pendientes.<br>2. El trabajador escoge un trabajo que se realiza en menos de 24 horas.<br>3. El trabajador cancela el trabajo.<br>4. El sistema verifica si faltan menos de 24 horas para el trabajo.<br>5. El sistema comprueba que faltan menos de 24 horas para el trabajo.<br>6. El sistema resta 0,5 puntos de la reputación del trabajador.<br>7. El sistema le envía una notificación al contratante de que el trabajo fue cancelado. |
| **Diagrama de caso de uso** | ![Diagrama caso uso - 11](../images/DCU-REQ-SRV-0011) |
| **Poscondiciones** | Se espera como resultado el trabajador con una reputación con 0,5 puntos menos y la notificación enviada al contratante. |

### 2.12 Requerimiento Funcional 12

| Campo | Descripción |
|------|-------------|
| **ID** | REQ-SRV-0012 |
| **Nombre del requerimiento** | Consultas de solicitudes |
| **Descripción** | El sistema debe permitir al contratante y al trabajador consultar el listado de sus solicitudes activas e historicas. |
| **Precondiciones** | Para que el sistema cumpla con este requerimiento, el usuario debe de haber iniciado sesión. |
| **Actor** | Contratante y trabajador |
| **Flujo principal** | 1. El contratante o trabajador van a su propio perfil.<br>2. El contratante o trabajador escoge la opción de ver sus solicitudes.<br>3. El sistema filtra las solicitudes que se pueden ver (todas menos las eliminadas).<br>4. El sistema le muestra las solicitudes al usuario que las está revisando. |
| **Diagrama de caso de uso** | ![Diagrama caso uso - 12](../images/DCU-REQ-SRV-0012) |
| **Poscondiciones** | Se espera como resultado que se puedan ver todas las solicitudes realizadas y que no se muestren las eliminadas. |