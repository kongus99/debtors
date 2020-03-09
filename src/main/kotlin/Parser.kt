import java.io.File
import java.io.IOException
import java.lang.Integer.parseInt
import java.net.URL

class Parser {
    data class Debt(val debtor: String, val creditor: String, val amount: Int, val currency: String)

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
//            val amount = getDefaultAmountFactory()
//                .setCurrency(getCurrency(split[3].toUpperCase()))
//                .setNumber(parseLong(split[2])).create()
            return Debt(split[0], split[1], parseInt(split[2]), split[3])

        }
    }
}
