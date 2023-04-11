# Prototipo de Interfaz Hombre-Máquina para Evaluación de Habla Neurodegenerativa

Este proyecto tiene como objetivo desarrollar una aplicación Android para la captura, almacenamiento y evaluación de grabaciones de voz breves, con el fin de determinar el estado neurológico de posibles pacientes/usuarios. 

La aplicación llamará a un motor de evaluación que devolverá una puntuación numérica, la cual se mostrará en una ventana de tipo numérico y un indicador visual que establecerá la pertenencia del patrón de escala a un grado específico de estado neurodegenerativo.

## Funcionalidades de la aplicación

- La aplicación está orientada a un único usuario, cuyo identificador se puede tomar del propio sistema Android, IMEI, etc. No obstante, se permitirá que el usuario modifique su código dentro de la aplicación.
- La aplicación permite realizar diversos ejercicios vocales, como vocal /a/ sostenida, ejercicio diadococinético con repetición múltiple de grupos silábicos (por ejemplo: /pa/-/ta/-/ka/) o pronunciación de una determinada frase. Para ello, habrá un menú de ejercicios con instrucciones básicas para cada uno.
- La aplicación gestiona la adquisición de audio de forma continua, analizando el nivel sonoro de cada fragmento y mostrando al usuario una indicación gráfica del volumen. Si la grabación tiene un nivel muy bajo de sonido o hay indicios de que pueda estar saturada, la aplicación solicitará repetir la grabación.
- Si la grabación es correcta, se almacenará en el dispositivo con un nombre que incluye el identificador de usuario, la fecha y la hora. A continuación, se llamará al módulo de análisis que devolverá un número entero y aleatorio en un rango entre 0 y 32, el cual se mostrará en pantalla.
- Otras funcionalidades opcionales de la aplicación incluyen la visualización de la lista de grabaciones, la evolución de las puntuaciones para cada tipo de ejercicio y otras opciones de configuración.

## Requisitos

- Android Studio API 33
- Java 11

## Instalación

1. Clonar el repositorio a su máquina local.
2. Abrir el proyecto en Android Studio.
3. Ejecutar el proyecto en un dispositivo o emulador de Android.

## Uso

1. Al abrir la aplicación, se mostrará un menú con los diferentes ejercicios disponibles.
2. Seleccionar el ejercicio deseado y seguir las instrucciones en pantalla para realizar la grabación.
3. Si la grabación es correcta, la puntuación se mostrará en pantalla.
4. Otras funcionalidades opcionales están disponibles en la pantalla principal de la aplicación.

## Contribución

Las contribuciones son bienvenidas y se aceptarán a través de pull requests en GitHub. Para contribuir al proyecto, siga estos pasos:

1. Haga un fork del repositorio.
2. Cree una rama para realizar sus cambios.
3. Realice sus cambios en la nueva rama.
4. Cree un pull request describiendo sus cambios y motivaciones para los mismos.

## Autores

- [Applepie](https://github.com/aplepie)
- [Marta](httpsmartaest)

## Licencia

Este proyecto está licenciado bajo la Licencia MIT. Consulte el archivo LICENSE para obtener más información.
