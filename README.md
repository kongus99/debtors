# Debt converter
This app convert debt list into reducible matrix, removing cycles and reducing the defined problem to two layer graph - one layer for debtors, one for creditors.
It supports the different currencies by converting them to EUR using ECB exchange rates.
## Building
To build this app just run 
  gradlew build
from the main folder
## Usage
The program is used by running the `Main` class. There is some code there already pointing to how it can be used. To sum it up, you need to specify the input and output file in the csv format. Then you can parse that file and run optimize on the matrix to reduce the complexity of the graph. The graph in this form can be printed out to console or saved to another file. To change default conversion you need to adjust the `DebtMatrix` file.
