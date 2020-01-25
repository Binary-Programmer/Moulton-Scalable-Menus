# Moulton Scalable Menus
A Java library for scalable menus. Essentially replaces Java AWT and Java Swing. Gives more freedom and minimalism with the GUI without the bulk of libraries like JavaFX.

Start by using making your menu container implement Container. Then subclass MenuManager for your specific needs. For the programmer's freedom, the MenuManager will not automatically register input events. Thus the programmer needs to redirect those events to the MenuManager where desired.
