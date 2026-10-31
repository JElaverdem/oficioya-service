# oficioya-service

## Requerimientos Funcionales - Dominio Service

| Campo | Descripción|
|---|---|
| **Proyecto** | OficioYa |
| **Squad** | 4 |
| **Dominio** | Service |
| **Product Owner** | Juan Jose Rivera |
| **Analista Funcional** | Santiago Gomez |
| **Lider Técnico** | Juan Esteban Laverde |
| **Equipo de Desarrollo** | Brian Fierro, Diego Mesa |

## Diagrama entidad-relación

![ER Service](/src/main/resources/docs/uml/ER-Service.png)

## Diagrama de clases

![Diagrama de clases Service](/src/main/resources/docs/uml/DiagramaClases-Service.png)

## Mock Up

### Link
[Link del figma](https://www.figma.com/make/wy8eVjfR1SbrWb3wUqMIkM/Sistema-de-calificaci%C3%B3n-5-estrellas?t=FlzxIc07XaISpXqc-20&fullscreen=1)

### Fotos
![1. Rellenar datos](/src/main/resources/docs/images/1.Rellenar%20datos.png)
![1. Escoger trabajadores](/src/main/resources/docs/images/1.Escoger%20trabajadores.png)
![1. Confirmar datos](/src/main/resources/docs/images/1.Confirmar%20datos.png)
![1. Solicitud enviada](/src/main/resources/docs/images/1.Solicitud%20enviada.png)
![2. Rellenar datos](/src/main/resources/docs/images/2.Rellenar%20datos.png)
![2. Confirmar datos](/src/main/resources/docs/images/2.Confirmar%20datos.png)
![2. Solicitud enviada](/src/main/resources/docs/images/2.Solicitud%20enviada.png)

## Documentación de la API (Swagger / OpenAPI)

Con la aplicación levantada en el perfil `dev`:

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8081/swagger-ui.html |
| OpenAPI JSON | http://localhost:8081/v3/api-docs |

El servicio **no exige autenticación**: Swagger no muestra botón Authorize ni
candados. Las rutas de documentación son públicas y en el perfil `prod` están
deshabilitadas desde `application-prod.properties`.

Para operar en nombre de un usuario se envía el header `X-User-Id` con su UUID:

1. Abrir Swagger UI.
2. Elegir el endpoint (por ejemplo `GET /api/v1/requests`).
3. Pulsar **Try it out**.
4. Escribir un UUID en el campo `X-User-Id`.
5. Pulsar **Execute**.

Si no se envía `X-User-Id` y tampoco hay un JWT válido en `Authorization`, la API
responde `400`.

## Colección de Postman

Archivos:

- `postman/OficioYa-Service.postman_collection.json`
- `postman/OficioYa-Local.postman_environment.json`

Pasos:

1. Importar la colección y el environment **OficioYa Local** en Postman.
2. Levantar el servicio en `http://localhost:8081` con el perfil `dev`.
3. En el environment, poner en `userId` el UUID del usuario que se quiere simular.
4. Ejecutar **Solicitudes > Listar mis solicitudes**: el script guarda el primer `id` en `{{requestId}}`.
5. Para aceptar o rechazar, `userId` debe ser el `workerId` de la solicitud.
6. Para resolver un conflicto, `userId` debe ser el `contractorId`.

La colección no usa tokens: todas las peticiones envían `X-User-Id: {{userId}}`.

> El login sigue disponible en `/api/v1/auth/login`, pero es opcional y no se usa
> en la colección.
