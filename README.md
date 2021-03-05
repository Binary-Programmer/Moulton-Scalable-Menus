# Moulton Scalable Menus
A Java library for scalable menus. Essentially replaces Java AWT and Java Swing. Gives more freedom and minimalism with the GUI without the bulk of libraries like JavaFX.

## Table of contents
* [Technologies](#technologies)
* [Setup](#setup)
* [Use](#use)
* [Examples](#examples)

## Technologies
The project requires a Java version of at least 1.7 to compile. Pre-compiled jars are also available for download.

## Setup
Download either the source .java files or select a version of the pre-compiled .jar files. Add Moulton Scalable Menus as a dependency to your intended project.

## Use
Moulton Scalable Menus *requires* two core features to be implemented by you to function properly. There must be a container (which implements the `moulton.scalable.containers.Container` interface) and a menu manager (which extends the abstract `moulton.scalable.containers.MenuManager` class).

### Container
The container is the class that is responsible for telling the menu how large it should be rendered. Since Moulton Scalable Menus is built on the principle of scalable menus, this container should be able to scale and accurately report the size back to the menu. The container must implement `getMenuWidth()` and `getMenuHeight()`, which the menu will call.

The container is usually the class that passes any necessary events (such as mouse clicking, mouse movement, and key typing) to the menu, although any class can perform this function. A full list of events that the menu manager can handle is listed in the following section.

#### Input Events
For the programmer's freedom, Moulton Scalable Menus will not automatically handle input events. The programmer may selectively filter out events as they choose, and they may choose an input approach. Therefore, if the menu system is to be responsive to the user's key input, the programmer must handle input events and redirect them to the menu manager.

The events that the menu manager can handle are these:
* mouse pressed (location x,y)
* mouse released (location x,y)
* mouse scrolled/wheel (location x,y, amount)
* mouse moved (location x,y)
* key typed (char)
* key pressed (key code)

### Menu Manager
The menu manager is in control of directly managing the menu system. The menu manager must create the components in the menu, handle events, and render the components onto a `java.awt.Graphics` object.

There are three abstract methods that the menu must implement to accomplish these tasks: `createMenu()`, `clickableAction(moulton.scalable.clickables.Clickable)`, and `lostFocusAction(moulton.scalable.clickables.Clickable)`. Understanding these three key methods is vital to making a responsive menu system.

#### Create Menu: `createMenu()`
This is where the menu manager expects you to create the menu. The menu system is built to handle a tree structure of menu components, each of which extend `moulton.scalable.utils.MenuComponent`. Panels may hold other components (such as buttons, text boxes, shapes, images, captions) and other panels. The menu manager has a root Panel called `menu`. For any component to be rendered on the menu, it must be included as a child component of menu. For a better overview of menu components, please read [Menu Components](./docs/MenuComponents.md).

The create menu method will not be called automatically when the Menu Manager is created. Thus, it *must* be called by the library user for the menu components to be created.

#### Clickable Action: `clickableAction(Clickable)`
Some components are inherently clickable. Clickable components like buttons and text boxes are intended to react when they are clicked by the user. When the menu manager identifies that the user has mouse pressed and mouse released on the same menu component, then that component is clicked. If that component is a subclass of `moulton.scalable.clickables.Clickable`, then it will be passed to the menu manager via this method. The programmer can then identify which component was clicked and perform the appropriate action here. (Note: each clickable component has a string
`id` field that can be very useful in determining the identity of the clickable.)

#### Lost Focus: `lostFocusAction(Clickable)`
Focus is initiated when the user mouse presses on a component. Most components lose focus when the mouse is released. However, some components (like text boxes), keep the focus. This can be useful for the user to pass key information to the component after the box has been selected by the clicking. In rare cases, components must perform an action when they lose focus.

## Examples
Example dummy projects have been provided to show off certain features and demonstrate intended use. There are currently seven examples. These examples are in no particular order.

### Example 1
Example 1 shows off the grid system of `moulton.scalable.containers.Panel` including column and row weights, frames, and margins. Example 1 also demonstrates the usage of pop ups.

### Example 2
Example 2 shows off virtual space of `moulton.scalable.containers.VirtualPanel` used with `moulton.scalable.draggables.ScrollBar`, invisible and polygonal buttons, images, and non-editable and variably editable text boxes.

Example 2 also shows how forms could be implemented (in the bottom right corner) by using the form chain and a `moulton.scalable.clickable.FormButton`.

### Example 3
Example 3 shows off the `moulton.scalable.texts.TextHistory`.

### Example 4
Example 4 shows off radio buttons, image buttons, and animations.

### Example 5
Example 5 shows off text boxes with virtual space, scrolling vertically or horizontally.

### Example 6
Example 6 shows off animated buttons and partitions. The partition is used to make the text scroll bar disappear when the text length is not sufficiently long, and reappear when the text in the box would need to take more space than available. The animated button is configured to react to button presses and to the animation's end.

### Example 7
Example 7 shows off pop ups and the TextEditBox, a subclass of TextBox that has extra functionality such as vertical arrow key support, shift selection, and control text navigation.