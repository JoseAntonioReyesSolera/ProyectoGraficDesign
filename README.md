# ProyectoGraficDesign

# Drawer
Introducció

OpenCVDrawing és una aplicació en Java que permet dibuixar sobre una imatge utilitzant OpenCV. L'usuari pot seleccionar colors, modificar la mida del pinzell, desfer i refer accions, i guardar la imatge resultant.

Requisits

Java Development Kit (JDK) instal·lat

OpenCV 4.9.0 amb la llibreria opencv_java490.dll

Una imatge per carregar (per defecte images/moon.jpg)

Instal·lació i Execució

Assegurar-se que OpenCV està instal·lat i que la DLL opencv_java490.dll està accessible.

Compilar el projecte amb javac -cp .;opencv-490.jar OpenCVDrawing.java

Executar amb java -cp .;opencv-490.jar com.mycompany.dibuixets.OpenCVDrawing

Funcionalitats Principals

Dibuix sobre la imatge: Amb el ratolí, es poden traçar línies a sobre de la imatge carregada.

Selecció de color: Un selector de color permet triar el color del pinzell.

Ajust de la mida del pinzell: Un slider permet modificar el gruix del traçat.

Guardar la imatge: La imatge editada es pot desar en un fitxer.

Desfer i refer accions: Funcions per revertir i recuperar canvis.

Manual d'Usuari

Obrir l'aplicació.

Dibuixar sobre la imatge amb el ratolí.

Canviar el color del pinzell amb el botó "Seleccionar Color".

Ajustar la mida del pinzell amb el control lliscant.

Guardar la imatge amb el botó "Guardar".

Utilitzar "Desfer" per tornar a un estat anterior.

Utilitzar "Refer" per recuperar una acció desfeta.
