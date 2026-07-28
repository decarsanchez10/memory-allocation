# Memory Allocator Simulator

A modern, premium desktop application built in Java that simulates and visualizes Memory Allocation algorithms (First-Fit and Best-Fit). It features a sleek, dark-themed dashboard UI tailored for a professional operating system monitoring experience.

## Features

- **Real-time Memory Visualization:** Watch how processes are dynamically allocated to memory blocks using a visual representation of memory bars.
- **Algorithm Comparison:** Side-by-side comparison of **First-Fit** vs **Best-Fit** allocation strategies.
- **Detailed Analytics:** View detailed statistics including total allocated memory, internal fragmentation, and external fragmentation.
- **Modern UI:** Built entirely in Java Swing but styled to look like a premium modern desktop application (similar to Windows 11 Settings, NZXT CAM, or MSI Center).
- **Interactive Configuration:** Customize memory blocks, processes, and animation speeds on the fly.

## Technologies & Tools Used

- **Java (JDK 8+)**: The core programming language used for both the logic and the graphical user interface.
- **Java Swing**: The standard GUI toolkit used to render the application. We implemented heavily customized `JPanel` and `JButton` subclasses to achieve rounded corners, gradients, and soft shadows without relying on web technologies like HTML/CSS.
- **[FlatLaf (Flat Look and Feel)](https://www.formdev.com/flatlaf/)**: A modern, open-source Look and Feel for Java Swing. Specifically, the `FlatDarkLaf` theme is used as the base to override outdated Java Swing defaults and provide a crisp, dark aesthetic, seamless typography, and better DPI scaling.

## Project Architecture

The project follows the classic **MVC (Model-View-Controller)** design pattern:

### 1. Model (`/model`)
Contains the core data structures and simulation logic. It is completely independent of the GUI.
- **`Simulator.java`**: The engine. Contains the implementation of the `firstFit()` and `bestFit()` algorithms. It calculates fragmentation and tracks which processes go where.
- **`MemoryBlock.java`**: Represents a chunk of physical memory, tracking its size and whether it's currently occupied by a process.
- **`Process.java`**: Represents a process requesting memory, tracking its requested size and allocation status.
- **`AllocationResult.java`**: A data transfer object that stores the final state of an allocation run (the state of all blocks and total fragmentation).

### 2. View (`/view`)
Contains all the graphical components. Everything here extends standard Swing components but overrides `paintComponent` to draw modern UI elements.
- **`MainDashboardView.java`**: The root window (`JFrame`). It uses a `CardLayout` to switch between the Dashboard, Results, and Settings pages.
- **`LeftSidebar.java`**: The interactive navigation menu on the left. It takes user input for memory sizes and allows triggering simulations.
- **`MemoryVisualizerPanel.java` & `MemoryBar.java`**: Renders the animated horizontal bars representing memory blocks and their filled states.
- **`ResultsPage.java`**: A detailed data table view showing the side-by-side metrics of the algorithms.
- **`SettingsPage.java`**: A configuration page to toggle algorithms and adjust animation speeds.
- **Custom UI Elements**: Classes like `RoundedPanel`, `RoundedButton`, `DashboardCard`, and `ModernUI` (a central UI token dictionary) handle the premium aesthetics (gradients, rounded corners, modern typography).

### 3. Controller (`/controller`)
- **`MainController.java`**: The bridge. It listens for button clicks from the View, reads the input data, passes it to the `Simulator` (Model), and then pushes the calculated results back to the View to trigger animations and update charts.

### 4. Entry Point (`App.java`)
- Initializes the `FlatDarkLaf` theme.
- Bootstraps the application by instantiating the View and the Controller, then makes the window visible.

## How to Run

1. Ensure you have the Java Development Kit (JDK) installed.
2. The project requires the `flatlaf-3.4.1.jar` dependency to run. Make sure it is in the root directory alongside `App.java`.
3. **Compile the code (Windows):**
   ```powershell
   javac -cp ".;flatlaf-3.4.1.jar" App.java model/*.java view/*.java controller/*.java
   ```
4. **Run the application (Windows):**
   ```powershell
   java -cp ".;flatlaf-3.4.1.jar" App
   ```

*(Note: On Mac/Linux environments, use `:` instead of `;` in the classpath: `-cp ".:flatlaf-3.4.1.jar"`)*
