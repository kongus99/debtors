class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val csv = Main::class.java.getResource("/input1.csv")
            val debts = Parser.fromCsv(csv)
            val matrix = DebtMatrix.fromDebts(debts)
            println(matrix.flatten())
            matrix.optimize()
            println(matrix.flatten())
        }
    }
}
