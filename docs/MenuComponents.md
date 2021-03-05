# Menu Components Overview
Each program and each menu may need a different set of menu components. Therefore, all components can be extended and modified by the programmer for specific needs. Nevertheless, a collection of the most commonly used components has been provided.

## Table of Contents
* [Positioning](#positioning)
  * [Expressions](#expressions)
  * [Grid](#grid)
  * [Virtual Space](#virtual-space)
* [Components](#components)
  * [Panels](#panels)
  * [Clickables](#clickables)
  * [Text Components](#text-components)
  * [Visuals](#visuals)
  * [Shapes](#shapes)
  * [Pop Ups](#pop-ups)

## Positioning
The premise of Moulton Scalable Menus is that the components are arbitrarily scalable. As such, positioning is of great importance. There are two systems established whereby components can be positioned, by expressions and by the panel grid. Each has its own pros and cons, so both systems can be used together in the same menu, even on the same panel.

When a component is created, it asks for its parent panel and asks for either strings of location or integers for location. The strings are to be string expressions. The integers are to be the component's position on the parent's grid. Regardless of positioning system, the component will add itself as a child of the parent automatically.

### Expressions
Components may be given string expressions that are evaluated at render time to determine where the component should be positioned. Expressions are evaluated in double float precision, but are rounded to the nearest integer to conform to pixel coordinates when the value is used. The top left corner of the screen is (0,0). The rightward direction is +x. The downward direction is +y. Expressions may use operators, literals, and a defined set of variables. There is also a wild character `?` implemented out of convenience in many components. This wild card is explained in more detail in a later section ([Wild Character](#wild-character)).

Expressions are expected in infix notation. Use parentheses `( )` as needed and as appropriate. Additionally, after tokenization, all whitespace is removed and ignored. Therefore, it is equivalent to write `3+4` or `3 + 4`. 

#### Operators
The operators are as follows:
* Addition (`+`)
* Subtraction (`-`): Also serves as the symbol for negation.
* Multiplication (`*`): When no operator is provided, multiplication is assumed. For example, `2pi` is evaluated as `2 * pi`.
* Division (`/`)
* Exponentiation (`^`)
* Root (`r`): As in the square root, which is denoted by `2r`. So to be explicit, the square root of 16 would be written as `2r16`.

Additionally, a select few functions may be utilized (Functions do not need parantheses for their argument):
* Trigonometric functions `cos`, `sin`, and `tan`. The argument is expected to be in radians.
* Logarithmic functions `log` and `ln`. These are the logarithms in base 10 and base e, respectively.
* Operator-style value selection functions `max` and `min`. The first parameter comes before the function name, and the second parameter comes after. For example, `5 max 6` would result to 6.

#### Literals
Literal numbers can be given as integers or as floats. Regardless of input, calculations will take place in double precision. Numbers can also be given in scientifc notation, following the form:
```
<significand> E <exponent>
```
Please note that:
* This is the form that Java doubles use for scientific notation. No conversion is necessary.
* The base, `E`, must be capitalized to distinguish it from the variable `e`.
* Negative exponents do not require parantheses. For example, `6.674E−11` is equivalent to `6.674E(-11)`.
* Spaces between the three parts may be added or removed to preference.

#### Variables
The following variables may be used in expressions:
* `width`: the width of the parent panel
* `height`: the height of the parent panel
* `centerx`: the horizontal center of the parent panel. Equivalent to `width/2`.
* `centery`: the vertical center of the parent panel. Equivalent to `height/2`.
* `pi`: the value of pi in double precision (3.1415926536)
* `e`: the value of e in double precision (2.7182818285)

#### Wild Character
The wild character, `?`, may also be used in some specific instances, as implemented by the menu component.

First, the wild character may be used in most rectangular components in the width or height field to indicate an endpoint. So if width =`?width/3`, the component would stretch from its x to a third the width of the parent panel. This is especially useful to mitigate rounding error.

Second, the wild character may be used in in inherently one-dimensional components (such as moulton.scalable.geometrics.Line) as a ditto. If the x2 or y2 fields are the wild character, then the component assumes that x2=x1 or y2=y1, respectively.

### Grid
Grids are often very useful in the design of a menu. (0,0) corresponds to the component at the top left corner of the grid. The rightward direction is +x. The downward direction is +y. Components can be added to any location on the grid (as long as the integer is 0 or positive) and the grid will automatically expand when necessary. Rows and columns do not need to be full. For example, if the only component in the grid was at (1,0), the grid would still be split in half horizontally.

There are some components (notably the shape components) that take both grid and expression fields. These components are encorporated in the grid as others, and the expressions are used to dictate their shape within their grid allotment.

There are several non-component factors to the rendering of the grid; Grids can have frames, margins, and weights.

#### Frame
Grid frames are on the external border. The frame determines how much space should be between the components inside the grid and the outside of the parent panel. The frame is divided into two parts: horizontal and vertical.

#### Margin
The grid margin is the space between components in the grid. The margin is divided into two parts: horizontal and vertical.

#### Weights
Row weights and column weights can be used to increase the size of specific rows or columns in the grid. By default, all rows and columns have a weight of 1.0. A row with a weight of 2.0 would have double the height of a row with a width of 1.0. Similarly, a column with a weight of 0.5 would have half the width of a column with a weight of 1.0.

### Virtual Space
Virtual space is a powerful concept where components exist within the menu design but are not necessarily visible to the user. An example of a component with virtual space is a small text box that holds many lines of text, but only the lines directly adjacent to where the user is entering data is visible. Components that use virtual space commonly use scroll bars to give the user access to the whole structure. The generic `moulton.scalable.containers.Panel` does not allow for virtual space, but the classes and subclasses of `moulton.scalable.containers.VirtualPanel`, `moulton.scalable.texts.TextBox`, and `moulton.scalable.texts.TextHistory` do.

## Components
Now that it has broadly been explained how the components work, an overview of component types available for use is given.

### Panels
Panels serve as the backbone for the tree structure of the menu. All panels extend `moulton.scalable.containers.Panel`, which provides basic panel functionality.

`moulton.scalable.containers.PartitionPanel` is a subclass of the Panel class with boundaries alterable during runtime. This contrasts from the regular Panel because these boundary changes occur outside of size changes, for example, if the user wanted to change the size of a panel relative to others. [Example 6](README.md#example-6) demonstrates the use of the `PartitionPanel` to make a disappearing and reappearing side scroll bar.

`moulton.scalable.containers.VirtualPanel` gives the programmer the ability to utilize virtual space for the menu design. These are best paired with horizontal and/or vertical scroll bars to let the user maneuver.

`moulton.scalable.containers.ListPanel` is a subclass of the VirtualPanel, specially designed to hold and display components in a dynamically sized list.

### Clickables
Foremost among clickables are buttons, which all inherit from the abstract class `moulton.scalable.clickables.RadioButton`. Don't be confused by the name: all buttons have the functionality in place to be radio buttons, but only if they are in a `moulton.scalable.clickables.RadioGroup`. The most commonly used button is `moulton.scalable.clickables.Button`, but there are also image buttons, animated buttons, and polygonal buttons.

### Text Components
There are text components like captions, which are simply static text, and text boxes, that can be modified by the user at runtime. Another useful component is the text history, which can display a dynamic list of messages.

Most text components have some concept of text alignment, which is represented by the `moulton.scalable.texts.Alignment` enum or line spanning, which can be accounted for by the helper class `LineBreak` in the same package.

`moulton.scalable.texts.TextBox` is especially customizable. Default behavior includes draggable text selection, text input of common Unicode characters, configurable hot keys such as copy, cut, paste, and select all, character masks, character filtering, virtual space, associated scroll bars, and more. If for whatever reason the provided text box does not meet implementation needs, a class need only extend `moulton.scalable.texts.TextInputComponent` to receive text data from the Manager.

### Visuals
There are components specifically designed to handle images or animations. There are also clickable button components corresponding to each of the static versions.

Java does not natively handle vector images such as those of .svg, .svgz, or .vstm format. As such, in an effort to limit other dependencies, Moulton Scalable Menus uses BufferedImages for image processing. However, this would not prevent the extension of `moulton.scalable.clickables.Clickable` or `moulton.scalable.utils.MenuComponent` for this very purpose.

### Shapes
Some menu components are specially suited to draw lines and arbitrary polygons. These classes are located in the `moulton.scalable.geometrics` package. Some classes in the package include Line, to draw a line of an arbitrary thickness (as set by a string expression) between a pair of points, Polygon, to draw a purely asthetic shape onto a panel, and PolygonalButton, to draw a button of whatever shape. Furthermore, `moulton.scalable.geometrics.ShapeResources` is provided to further aid in the construction of geometric menu components. This class provides helpful generation methods, such as `generateCircleXCoords(String centerX, String radius, int precision)` and useful predetermined shape constants such as `TRIANGLE_HORIZ_YS`.

### Pop Ups
Although pop ups do not generally fit the mold of scalable components, there is an available framework for pop up components. Unlike regular components, pop ups need to be added directly to the menu manager as the `popup` field. These pop ups have an associated root panel `base`, which can be built off of just like the regular menu root.

All pop ups must be descendants of the class `moulton.scalable.popups.Popup`. Due to the sheer number of possibilities and varying implementation strategies, said pop up parent class provides minimal features. These features include dimensions, the aforementioned Panel base, touchCheckList (which is a list structure of components that need to be checked for mouse interactivity), and a background color.

Two pop up subclasses have also been provided in the package to accomodate common usage. These are the aptly named `NotificationPopup` and `ConfirmationPopup`.
