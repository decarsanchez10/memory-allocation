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

All classes across the Model, View, and Controller layers have been consolidated into a single, clean Java source file:

- **`App.java`**: Self-contained single source file containing:
  - **`App`**: Main entry point initializing FlatLaf and bootstrapping the application.
  - **Model Classes**: `Simulator`, `MemoryBlock`, `Process`, `AllocationResult`.
  - **Controller Class**: `MainController`.
  - **View Classes**: `MainDashboardView`, `LeftSidebar`, `ProcessQueuePanel`, `MemoryVisualizerPanel`, `MemoryBar`, `SummaryCardsPanel`, `DashboardCard`, `ResultsPage`, `ResultsTabPane`, `SettingsPage`, `ModernTable`, `StatusBadge`, `SidebarButton`, `LegendPanel`, `RoundedPanel`, `RoundedButton`, `RoundedTextField`, `ModernUI`.

## How to Run

1. Ensure you have the Java Development Kit (JDK) installed.
2. The project requires the `flatlaf-3.4.1.jar` dependency to run. Make sure it is in the root directory alongside `App.java`.
3. **Compile the code (Windows):**
   ```powershell
   javac -cp ".;flatlaf-3.4.1.jar" App.java
   ```
4. **Run the application (Windows):**
   ```powershell
   java -cp ".;flatlaf-3.4.1.jar" App
   ```

*(Note: On Mac/Linux environments, use `:` instead of `;` in the classpath: `-cp ".:flatlaf-3.4.1.jar"`)*
