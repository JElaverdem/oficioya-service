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

Las rutas de documentación son públicas (`SecurityConfig` las deja pasar sin JWT).
En el perfil `prod` están deshabilitadas desde `application-prod.properties`.

Para probar los endpoints protegidos:

1. Abrir Swagger UI.
2. Ejecutar `POST /api/v1/auth/login`.
3. Copiar el `accessToken` de la respuesta.
4. Pulsar **Authorize** y pegar el token (el esquema `bearerAuth` ya antepone `Bearer`).

## Colección de Postman

Archivos:

- `postman/OficioYa-Service.postman_collection.json`
- `postman/OficioYa-Local.postman_environment.json`
- `postman/generate-dev-tokens.ps1`

Pasos:

1. Importar la colección y el environment **OficioYa Local** en Postman.
2. Levantar el servicio en `http://localhost:8081` con el perfil `dev`.
3. Ejecutar **Auth > Login (CONTRATANTE)**: el script de test guarda el token en `{{token}}`.
4. Ejecutar **Solicitudes > Listar mis solicitudes**: el script guarda el primer `id` en `{{requestId}}`.
5. Para aceptar o rechazar se necesita un token con rol `TRABAJADOR`.

El login actual es de laboratorio: ignora las credenciales y siempre emite un token
`CONTRATANTE`. Por eso el token de trabajador se genera aparte, con el mismo secreto
de desarrollo:

```powershell
.\postman\generate-dev-tokens.ps1
```

El script imprime `CONTRATANTE=...` y `TRABAJADOR=...`. Copiar el valor de
`TRABAJADOR` en la variable de entorno `workerToken` de Postman.

También se puede generar un token para otro rol o con otra vigencia:

```powershell
.\postman\generate-dev-tokens.ps1 -Role ADMIN -TtlMinutes 60
```

### Limitación actual para aceptar o rechazar

El login crea un **UUID nuevo en cada llamada**, así que el usuario del token
`CONTRATANTE` no coincide con ninguna solicitud guardada y el listado suele salir
vacío. Para probar aceptar, rechazar o resolver un conflicto hay que usar el UUID
que ya existe en la base de datos (`request.workerId` o `request.contractorId`):

```powershell
.\postman\generate-dev-tokens.ps1 -Role TRABAJADOR -UserId <uuid-del-trabajador>
.\postman\generate-dev-tokens.ps1 -Role CONTRATANTE -UserId <uuid-del-contratante>
```

Sin `-UserId` el script usa un UUID aleatorio, útil solo para comprobar 401/403.

> El script usa `JWT_SECRET` si está definido; si no, usa el secreto por defecto de
> `application.properties`. No debe usarse para generar tokens en producción.