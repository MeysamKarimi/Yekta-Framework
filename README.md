# Yekta-Framework
Welcome to Yekta framework! This tool is designed to generate models using the meta-heuristic algorithms "Ant-Colony Optimization" (ACO) and "Gray-Wolf Optimization" (GWO). It also integrates a random generator. The tool helps to generate and manipulating models based on user-defined metamodels, OCL constraints, and optional algorithm-specific inputs. Whether you are a researcher, developer, or tester, this tool simplifies the process of generating models for your projects.

## Table of Contents
- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [Class Diagram](#class-diagram)
- [Contributing](#contributing)
- [License](#license)

## Overview

Yekta framework simplifies the model generation process by providing a user-friendly interface to create test models. It supports three algorithms: ACO (Ant Colony Optimization), GWO (Grey Wolf Optimization), and a random procedure, enabling users to generate models based on specific algorithmic strategies. 
Yekta is a powerful tool for generating models and has demonstrated remarkable effectiveness in mutation testing analysis, so it is specially tailored to model-based testing. In our evaluations [1], we found that approximately 80% of mutations, created by Guerra and et al. [3], were successfully killed using ACO algorithm. This exceptional mutation-killing rate underscores the tool's robustness and utility for software testing and quality assurance.


## Prerequisites

Before you begin, make sure you have the following prerequisite installed:

- [Java 11](https://docs.oracle.com/en/java/javase/11/install/#Java-Platform%2C-Standard-Edition)

## Getting Started

To get started with Yekta framework, follow these steps:

1. **Clone**:
   - Clone the repository to your local machine.
   - Open the project in your preferred Java IDE.
2. **Setup**:
   - Set up your project configurations and ensure all prerequisites are met, including Java 11 and the required dependencies (e.g., MDE testing framework, anATLyzer, ATL).
   1. **Maven Build (POM)**:
      - If your project uses Maven (POM), you can build and manage dependencies using Maven. Open a terminal in the project directory and run the following command:
        ```shell
        mvn clean install
        ```
        This command will build your project and download any required dependencies.
   2. **IDE-Specific Setup**:
      - If your IDE supports Maven integration, it should automatically recognize the POM configuration and synchronize the project. Ensure that your IDE is properly configured for Maven.
3. **Run the Project**:
   - After building the project, you can run it from your IDE, following the standard Java application execution procedures.

Now you're ready to use Yekta framework to create and manipulate test models with ACO, GWO, and Random algorithms.


## Usage

Once you have Yekta framework up and running, follow these steps to create test models:

1. **Metamodel and OCL Files**: Provide your metamodel and the optional well-formed OCL constraints expressed on the metamodel as input to the tool.
2. **Select Algorithm**: Choose one of the supported algorithms: ACO, GWO, or Random.
3. **Algorithm-Specific Inputs**: Depending on the selected algorithm, provide the necessary algorithm-specific inputs. Consider the fact that the number of agents specified by "Population size" parameter, must be bigger or equal to the number of models you like to generate specified by "Model Count" parameter.
4. **Run**: Click the "Run" button to initiate the model generation process. 
5. **Result**: The framework will generate models with the specified number of EObjects based on your inputs for each model in the folder called "model" in the main package.

## Class Diagram

The class diagram for Yekta framework is provided below for a better understanding of the project's structure.
For more information please see our attached paper [2].
![alt text](https://github.com/MeysamKarimi/Yekta-Framework/blob/main/doc/Class-diagram.png)

## Contributing

We welcome contributions to Yekta framework. If you'd like to contribute, please follow these guidelines:

1. Fork the repository.
2. Create a new branch for your feature or bug fix.
3. Commit your changes with clear messages.
4. Submit a pull request.

## License

This project is licensed under the MIT Licens - see the [MIT](https://github.com/git/git-scm.com/blob/main/MIT-LICENSE.txt) file for details.

## References
[1] Meysam Karimi, Shekoufeh Kolahdouz-Rahimi, Javier Troya. "Ant-colony optimization for automating test model generation in model transformation testing". Journal of Systems and Software, Volume 208, 1--16 2024. doi: 10.1016/j.jss.2023.111882

[2] **TBC [Submitted]:** Meysam Karimi, Shekoufeh Kolahdouz-Rahimi, Javier Troya. "Yekta: A framework for automated test models generation applying meta-heuristic algorithms". SoftwareX, 2023.

[3] Esther Guerra, Jesús Sánchez Cuadrado, Juan de Lara. Towards Effective Mutation Testing for ATL. In 22nd ACM/IEEE International Conference on Model Driven Engineering Languages and Systems, MODELS 2019, Munich, Germany, September 15-20, 2019 (pp. 78–88). IEEE. doi: 10.1109/MODELS.2019.00-13.

[4] J. S´anchez Cuadrado, E. Guerra, and J. de Lara, “Static analysis of model transformations,” IEEE Trans. Software Eng., vol. 43, no. 9, pp. 868–897, 2017.


