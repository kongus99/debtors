import Parser.Debt
import java.util.*
import kotlin.math.abs

class DebtMatrix(private val indexes: SortedMap<String, Int>, private val debtsMatrix: Array<Array<Int>>) {

    companion object {
        fun fromDebts(debts: List<Debt>): DebtMatrix {
            val indexes = calculateIndexes(debts)
            return DebtMatrix(indexes, debtsMatrix(indexes, debts))
        }

        private fun debtsMatrix(indexes: SortedMap<String, Int>, debts: List<Debt>): Array<Array<Int>> {
            val array = Array(indexes.size) { Array(indexes.size) { 0 } }
            debts.forEach { array[indexes[it.debtor] ?: 0][indexes[it.creditor] ?: 0] += it.amount }
            return array
        }

        private fun calculateIndexes(debts: List<Debt>): SortedMap<String, Int> {
            return debts
                .flatMap { listOf(it.debtor, it.creditor) }
                .toSet()
                .sorted()
                .mapIndexed { i, v -> Pair(v, i) }
                .toMap(TreeMap())
        }
    }

    fun optimize() {
        indexes.forEach { optimize(it.value) }
    }

    private fun optimize(person: Int) {
        var debtIndex = nextColumn(person, -1)
        var creditIndex = nextRow(-1, person)
        while (debtIndex != null && creditIndex != null) {
            val diff = debtsMatrix[creditIndex][person] - debtsMatrix[person][debtIndex]
            when {
                diff == 0 -> {
                    redirectCredit(debtIndex, creditIndex, debtsMatrix[person][debtIndex])
                    debtIndex = annulDebt(person, debtIndex)
                    creditIndex = annulCredit(creditIndex, person)
                }
                diff > 0 -> {
                    redirectCredit(debtIndex, creditIndex, debtsMatrix[person][debtIndex])
                    debtIndex = annulDebt(person, debtIndex)
                    debtsMatrix[creditIndex][person] = abs(diff)
                }
                diff < 0 -> {
                    redirectCredit(debtIndex, creditIndex, debtsMatrix[creditIndex][person])
                    debtsMatrix[person][debtIndex] = abs(diff)
                    creditIndex = annulCredit(creditIndex, person)
                }
            }
        }
    }

    private fun redirectCredit(debtIndex: Int, creditIndex: Int, value: Int) {
        if (creditIndex == debtIndex) return
        debtsMatrix[creditIndex][debtIndex] += value
    }

    private fun annulCredit(creditIndex: Int, person: Int): Int? {
        debtsMatrix[creditIndex][person] = 0
        return nextRow(creditIndex, person)
    }

    private fun annulDebt(person: Int, debtIndex: Int): Int? {
        debtsMatrix[person][debtIndex] = 0
        return nextColumn(person, debtIndex)
    }

    private fun nextColumn(row: Int, current: Int): Int? {
        for (i in current + 1 until debtsMatrix.size)
            if (debtsMatrix[row][i] > 0) return i
        return null
    }

    private fun nextRow(current: Int, column: Int): Int? {
        for (i in current + 1 until debtsMatrix.size)
            if (debtsMatrix[i][column] > 0) return i
        return null
    }

    fun flatten(): List<Debt> {
        val indexArray = indexes.keys.toTypedArray()
        return indexes.flatMap { e ->
            debtsMatrix[e.value].mapIndexed { i, v ->
                if (v > 0)
                    Debt(e.key, indexArray[i], v, "eur")
                else null
            }
        }.mapNotNull { it }
    }


    override fun toString(): String {
        return "DebtMatrix(indexes=$indexes, debts=${debtsMatrix.map { it.contentToString() }})"
    }


}
