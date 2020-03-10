import Parser.Debt
import org.javamoney.moneta.FastMoney
import java.util.*
import javax.money.MonetaryAmount
import javax.money.convert.MonetaryConversions

class DebtMatrix(
    private val indexes: SortedMap<String, Int>,
    private val debtsMatrix: Array<Array<MonetaryAmount>>
) {

    companion object {
        private const val DEFAULT_CURRENCY = "EUR"
        private val ECB = MonetaryConversions.getExchangeRateProvider("ECB")
        private val ZERO: MonetaryAmount = FastMoney.of(0, DEFAULT_CURRENCY)

        fun fromDebts(debts: List<Debt>): DebtMatrix {
            val indexes = calculateIndexes(debts)
            return DebtMatrix(indexes, debtsMatrix(indexes, debts))
        }

        private fun debtsMatrix(indexes: SortedMap<String, Int>, debts: List<Debt>): Array<Array<MonetaryAmount>> {
            val array =
                Array(indexes.size) { Array(indexes.size) { ZERO } }
            debts.forEach {
                val i = indexes[it.debtor] ?: 0
                val j = indexes[it.creditor] ?: 0
                array[i][j] = array[i][j]
                    .with(ECB.getCurrencyConversion(DEFAULT_CURRENCY))
                    .add(it.amount)
            }
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
            val diff = debtsMatrix[creditIndex][person].subtract(debtsMatrix[person][debtIndex])
            when {
                diff.isZero -> {
                    redirectCredit(debtIndex, creditIndex, debtsMatrix[person][debtIndex])
                    debtIndex = annulDebt(person, debtIndex)
                    creditIndex = annulCredit(creditIndex, person)
                }
                diff.isPositive -> {
                    redirectCredit(debtIndex, creditIndex, debtsMatrix[person][debtIndex])
                    debtIndex = annulDebt(person, debtIndex)
                    debtsMatrix[creditIndex][person] = diff.abs()
                }
                diff.isNegative -> {
                    redirectCredit(debtIndex, creditIndex, debtsMatrix[creditIndex][person])
                    debtsMatrix[person][debtIndex] = diff.abs()
                    creditIndex = annulCredit(creditIndex, person)
                }
            }
        }
    }

    private fun redirectCredit(debtIndex: Int, creditIndex: Int, value: MonetaryAmount) {
        if (creditIndex == debtIndex) return
        debtsMatrix[creditIndex][debtIndex] = debtsMatrix[creditIndex][debtIndex].add(value)
    }

    private fun annulCredit(creditIndex: Int, person: Int): Int? {
        debtsMatrix[creditIndex][person] = ZERO
        return nextRow(creditIndex, person)
    }

    private fun annulDebt(person: Int, debtIndex: Int): Int? {
        debtsMatrix[person][debtIndex] = ZERO
        return nextColumn(person, debtIndex)
    }

    private fun nextColumn(row: Int, current: Int): Int? {
        for (i in current + 1 until debtsMatrix.size)
            if (debtsMatrix[row][i].isPositive) return i
        return null
    }

    private fun nextRow(current: Int, column: Int): Int? {
        for (i in current + 1 until debtsMatrix.size)
            if (debtsMatrix[i][column].isPositive) return i
        return null
    }

    fun flatten(): List<Debt> {
        val indexArray = indexes.keys.toTypedArray()
        return indexes.flatMap { e ->
            debtsMatrix[e.value].mapIndexed { i, v ->
                if (v.isPositive)
                    Debt(e.key, indexArray[i], v)
                else null
            }
        }.mapNotNull { it }
    }


    override fun toString(): String {
        return "DebtMatrix(indexes=$indexes, debts=${debtsMatrix.map { it.contentToString() }})"
    }


}
