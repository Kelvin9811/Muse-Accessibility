# Muse Accessibility
Se detallan los cambios realizados dentro del proyecto Muse Accesibility
## Detalles de la version 1.0.0



### Features

- Se añade la grabación dinámica de la base de datos removiendo el ultimo elemento de la cola y añadiendo al inicio las nuevas muestras. Esto con el objetivo de tener una calibración no rejida a un número de parpadeos.
- Se añade la pantalla de testeo con el objetivo de probar el clasificador.
- Se controla el tamaño automatico de los textos.
- Se retira el spinner para el control de la sensibilidad al captar parpadeos.
- Se retira el spinner para el control de el minimo porcentaje a cumplir para determinar un parpadeo.
- Cambios generales de aspecto.


## Detalles de la version 2.0.0



### Features

- Se añade la pantalla de configuración. Por medio de esta pantalla el usuario tendrá acceso a configurar las opciones: 
- Sensibilidad al clasificar un parpadeo.
- Canal de interes.
- Sensibilidad al detectar un parpadeo.
- Número de vecinos cercanos.
- Las configuraciónes se guardarán en un archivo local. Al desisntalar la aplicación o eliminar el archivo se perderan las configuraciones y volveran al estado por defecto.
