import org.javamoney.moneta.FastMoney
import java.io.File
import java.io.IOException
import java.lang.Long.parseLong
import java.net.URL
import javax.money.MonetaryAmount

class Parser {
    data class Debt(val debtor: String, val creditor: String, val amount: MonetaryAmount) {
        fun toCSV(): String {
            return "${debtor}, ${creditor}, ${amount.number.longValueExact()}, ${amount.currency.currencyCode.toLowerCase()}"
        }
    }

    companion object {
        fun fromCsv(path: URL): List<Debt> {
            val debts = ArrayList<Debt>()
            File(path.toURI()).forEachLine { debts.add(parse(it)) }
            return debts
        }

        private fun parse(line: String): Debt {
            val split = line.split(",").map { it.trim() }
            if (split.size != 4 || split.contains(""))
                throw IOException("Incorrect number of entries")
            val amount = FastMoney.of(parseLong(split[2]), split[3].toUpperCase())
            return Debt(split[0], split[1], amount)
        }

        fun save(debts: List<Debt>, path: URL) {
            val writer = File(path.toURI()).bufferedWriter()
            debts.forEach {
                writer.write(it.toCSV())
                writer.newLine()
            }
            writer.flush()
            writer.close()
        }
    }
}
