import java.util.Optional
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrDefault

enum class Importance {
    IsRequired,
    IsOptional
}

enum class Representation {
    None,
    Any,
    Same,
    Str,
}

fun Boolean.toInt() = if (this) 1 else 0

public class Representator (val r: Representation, val s: String?) {
    fun get(): Representation {
        return this.r
    }
    fun get_str(): String {
        return when (this.r) {
            Representation.Str -> this.s!!
            else -> throw Exception("No string representation for ${this.r}!")
        }
    }
    companion object {
        fun None() = Representator(Representation.None, null)
        fun Any() = Representator(Representation.Any, null)
        fun Same() = Representator(Representation.Same, null)
        fun Str(s: String) = Representator(Representation.Str, s)
    }
}

enum class SqlTokens(public val repr: Representator, public val allowedFollowers: Array<Pair<Array<SqlTokens>, Importance>>) {
    WITH_ROLLUP(Representator.Str("WITH ROLLUP"), arrayOf()),
    POSITION(Representator.None(), arrayOf()),
    COMMA(Representator.Str(","), arrayOf()),
    QUOTE(Representator.Str("'"), arrayOf()),
    ANY_STR(Representator.Any(), arrayOf()),
    ANY_STR_NEXT(Representator.None(), arrayOf(
        Pair(arrayOf(COMMA), Importance.IsRequired),
        Pair(arrayOf(ANY_STR), Importance.IsRequired),
        Pair(arrayOf(ANY_STR_NEXT), Importance.IsOptional),
    )),
    ANY_STR_SEQ(Representator.None(), arrayOf(
        Pair(arrayOf(ANY_STR), Importance.IsRequired),
        Pair(arrayOf(ANY_STR_NEXT), Importance.IsOptional),
    )),
    QUOTED_STR(Representator.None(), arrayOf(
        Pair(arrayOf(QUOTE), Importance.IsRequired),
        Pair(arrayOf(ANY_STR), Importance.IsRequired),
        Pair(arrayOf(QUOTE), Importance.IsRequired),
    )),
    EQUAL(Representator.Str("="), arrayOf()),
    LESS(Representator.Str("<"), arrayOf()),
    MORE(Representator.Str(">"), arrayOf()),
    AND(Representator.Same(), arrayOf()),
    OR(Representator.Same(), arrayOf()),
    EXPR(Representator.None(), arrayOf(
        Pair(arrayOf(ANY_STR, QUOTED_STR), Importance.IsRequired),
        Pair(arrayOf(MORE,LESS,EQUAL), Importance.IsRequired),
        Pair(arrayOf(ANY_STR, QUOTED_STR), Importance.IsRequired),
    )),
    EXPR_NEXT(Representator.None(), arrayOf(
        Pair(arrayOf(AND,OR), Importance.IsRequired),
        Pair(arrayOf(EXPR), Importance.IsRequired),
        Pair(arrayOf(EXPR_NEXT), Importance.IsOptional),
    )),
    EXPR_SEQ(Representator.None(), arrayOf(
        Pair(arrayOf(EXPR), Importance.IsRequired),
        Pair(arrayOf(EXPR_NEXT), Importance.IsOptional),
    )),
    OFFSET(Representator.Str("OFFSET"), arrayOf()),
    SC_LEFT(Representator.None(), arrayOf()),
    SC_RIGHT(Representator.None(), arrayOf()),
    DESC(Representator.Same(), arrayOf()),
    ASC(Representator.Same(), arrayOf()),
    AS(Representator.Same(), arrayOf()),
    NUMBER(Representator.None(), arrayOf()),
    COL_NAME(Representator.Any(), arrayOf()),
    CHAR_SET_EXPR(Representator.Str("CHARACTER SET"), arrayOf(
        Pair(arrayOf(ANY_STR), Importance.IsRequired),
    )),
    INTO_OUTFILE(Representator.Str("INTO OUTFILE"), arrayOf(
        Pair(arrayOf(QUOTED_STR), Importance.IsRequired),
        Pair(arrayOf(CHAR_SET_EXPR), Importance.IsOptional),
        Pair(arrayOf(ANY_STR), Importance.IsRequired),
    )),
    VARNAME_EXPR(Representator.None(), arrayOf(
        Pair(arrayOf(ANY_STR), Importance.IsRequired),
        Pair(arrayOf(VARNAME_EXPR), Importance.IsOptional),
    )),
    INTO_DUMPFILE(Representator.Str("INTO DUMPFILE"), arrayOf(Pair(arrayOf(QUOTED_STR), Importance.IsRequired),)),
    INTO_VARNAME(Representator.Str("INTO"), arrayOf(Pair(arrayOf(VARNAME_EXPR), Importance.IsRequired),)),
    INTO_OPTION(Representator.None(), arrayOf(Pair(arrayOf(INTO_OUTFILE, INTO_DUMPFILE, INTO_VARNAME), Importance.IsRequired))),
    RC_OFFSET_OPTVAL(
        Representator.None(), arrayOf(
            Pair(arrayOf(NUMBER), Importance.IsRequired),
            Pair(arrayOf(COMMA), Importance.IsRequired),
            )
    ),
    RC_OFFSET_RQ(Representator.None(), arrayOf(
        Pair(arrayOf(NUMBER), Importance.IsRequired),
        Pair(arrayOf(OFFSET), Importance.IsRequired),
        Pair(arrayOf(NUMBER), Importance.IsRequired),
        )),
    RC_OFFSET_OPT(
        Representator.None(), arrayOf(
            Pair(arrayOf(RC_OFFSET_OPTVAL), Importance.IsOptional),
            Pair(arrayOf(NUMBER), Importance.IsRequired),
            )
    ),
    UPDATE(Representator.Same(), arrayOf()),
    SHARE(Representator.Same(), arrayOf()),
    NOWAIT(Representator.Same(), arrayOf()),
    LOCK_SHARED_MODE(Representator.Str("LOCK IN SHARE MODE"), arrayOf()),
    SKIP_LOCKED(Representator.Str("SKIP LOCKED"), arrayOf()),
    OF(Representator.Same(), arrayOf(Pair(arrayOf(ANY_STR_SEQ), Importance.IsRequired))),//
    FOR(Representator.Same(), arrayOf(
        Pair(arrayOf(UPDATE, SHARE), Importance.IsRequired),
        Pair(arrayOf(OF), Importance.IsOptional),
        Pair(arrayOf(NOWAIT, SKIP_LOCKED), Importance.IsOptional),
        Pair(arrayOf(LOCK_SHARED_MODE), Importance.IsOptional),
    )),
    LIMIT_EXPR(Representator.None(), arrayOf(
        Pair(arrayOf(RC_OFFSET_RQ, RC_OFFSET_OPT), Importance.IsRequired),
    )),
    LIMIT(Representator.Same(), arrayOf(Pair(arrayOf(LIMIT_EXPR), Importance.IsRequired),)),
    GROUP_BY_EXPR(
        Representator.None(), arrayOf(
            Pair(arrayOf(ANY_STR, EXPR_SEQ, POSITION), Importance.IsRequired),
            Pair(arrayOf(GROUP_BY_EXPR), Importance.IsOptional),
        )
    ),
    GROUP_BY(
        Representator.Str("GROUP BY"), arrayOf(
            Pair(arrayOf(GROUP_BY_EXPR), Importance.IsRequired),
            Pair(arrayOf(WITH_ROLLUP), Importance.IsOptional))
    ),
    WHERE_CONDITION(Representator.None(), arrayOf(Pair(arrayOf(EXPR_SEQ), Importance.IsRequired))),
    COUNT(Representator.Same(), arrayOf(
        Pair(arrayOf(SC_LEFT), Importance.IsRequired),
        Pair(arrayOf(ANY_STR), Importance.IsRequired),
        Pair(arrayOf(SC_RIGHT), Importance.IsRequired),
    )),
    ORDER_BY_EXPR(
        Representator.None(), arrayOf(
            Pair(arrayOf(COL_NAME, COUNT, POSITION), Importance.IsRequired),
            Pair(arrayOf(ASC, DESC), Importance.IsOptional),
            Pair(arrayOf(ORDER_BY_EXPR), Importance.IsOptional),
        )
    ),
    ORDER_BY(
        Representator.Str("ORDER BY"), arrayOf(
            Pair(arrayOf(ORDER_BY_EXPR), Importance.IsRequired),
            Pair(arrayOf(WITH_ROLLUP), Importance.IsOptional),
        )
    ),
    WINDOW_EXPR(
        Representator.None(), arrayOf(
            Pair(arrayOf(ANY_STR), Importance.IsRequired),
            Pair(arrayOf(AS), Importance.IsRequired),
            Pair(arrayOf(SC_LEFT), Importance.IsRequired),
            Pair(arrayOf(ANY_STR), Importance.IsRequired),
            Pair(arrayOf(SC_RIGHT), Importance.IsRequired),
            Pair(arrayOf(WINDOW_EXPR), Importance.IsOptional),
            )
    ),
    WINDOW(
        Representator.Same(), arrayOf(
            Pair(arrayOf(WINDOW_EXPR), Importance.IsRequired),
            )
    ),
    HAVING(Representator.Same(), arrayOf(Pair(arrayOf(WHERE_CONDITION), Importance.IsRequired))),
    WHERE(Representator.Same(), arrayOf(Pair(arrayOf(WHERE_CONDITION), Importance.IsRequired))),
    PARTITION_LIST(Representator.Any(), arrayOf()),
    PARTITION(Representator.Same(), arrayOf(Pair(arrayOf(PARTITION_LIST), Importance.IsRequired))),
    FROM(Representator.Same(), arrayOf(
        Pair(arrayOf(ANY_STR_SEQ), Importance.IsRequired),
        Pair(arrayOf(PARTITION), Importance.IsOptional)
    )),
    SQL_CALC_FOUND_ROWS(Representator.Same(), arrayOf()),
    SQL_NO_CACHE(Representator.Same(), arrayOf()),
    SQL_BUFFER_RESULT(Representator.Same(), arrayOf()),
    SQL_BIG_RESULT(Representator.Same(), arrayOf()),
    SQL_SMALL_RESULT(Representator.Same(), arrayOf()),
    HIGH_PRIORITY(Representator.Same(), arrayOf()),
    DISTINCTROW(Representator.Same(), arrayOf()),
    DISTINCT(Representator.Same(), arrayOf()),
    ALL(Representator.Same(), arrayOf()),
    SELECT(
        Representator.Same(), arrayOf(
            Pair(arrayOf(ALL, DISTINCT, DISTINCTROW), Importance.IsOptional),
            Pair(arrayOf(HIGH_PRIORITY), Importance.IsOptional),
            Pair(arrayOf(SQL_SMALL_RESULT), Importance.IsOptional),
            Pair(arrayOf(SQL_BIG_RESULT), Importance.IsOptional),
            Pair(arrayOf(SQL_BUFFER_RESULT), Importance.IsOptional),
            Pair(arrayOf(SQL_NO_CACHE), Importance.IsOptional),
            Pair(arrayOf(SQL_CALC_FOUND_ROWS), Importance.IsOptional),
            Pair(arrayOf(ANY_STR_SEQ), Importance.IsRequired),
            Pair(arrayOf(INTO_OPTION), Importance.IsOptional),
            Pair(arrayOf(FROM), Importance.IsOptional),
            Pair(arrayOf(WHERE), Importance.IsOptional),
            Pair(arrayOf(GROUP_BY), Importance.IsOptional),
            Pair(arrayOf(HAVING), Importance.IsOptional),
            Pair(arrayOf(WINDOW), Importance.IsOptional),
            Pair(arrayOf(ORDER_BY), Importance.IsOptional),
            Pair(arrayOf(LIMIT), Importance.IsOptional),
            Pair(arrayOf(INTO_OPTION), Importance.IsOptional),
            Pair(arrayOf(FOR), Importance.IsOptional),
            Pair(arrayOf(INTO_OPTION), Importance.IsOptional),
        ));
    private lateinit var childTokens: MutableList<SqlTokens>
    private lateinit var stringBody: String
    fun set(s: String) {
        println("Set $this to $s")
        stringBody = s
    }
    fun get(): String {return stringBody}

    fun addChild(t: SqlTokens) {
        childTokens.add(t)
    }
    fun getChildren(): Stream<SqlTokens> {
        return childTokens.stream()
    }
    private fun toPrettyString(): String {
        return when (this.repr.r) {
            Representation.None -> "--${this}"
            Representation.Any -> this.get()
            Representation.Same -> this.toString()
            Representation.Str -> this.repr.get_str()
        }
    }
    fun printAsATree(offset: Int){
        println(" ".repeat(offset) + this.toPrettyString())
        for (c in this.childTokens) {
            c.printAsATree(offset + 2)
        }
    }

    fun matchString(s: List<String>, currentOffset: Int): Pair<Optional<SqlTokens>, Int> {
        val tokensRange = currentOffset until s.size
        print("Try to find $this in ${s.slice(tokensRange)}... ")
        if(s.size <= currentOffset) return Pair(Optional.empty(), currentOffset)
        return when(repr.r) {
            Representation.None -> {
                println("going further.")
                this.matchFollowers(s, currentOffset)
            }
            Representation.Any -> {
                var res = s[currentOffset].matches("\\w+?".toRegex())
                return if(res) {
                    println("${s[currentOffset]} detected as $this")
                    this.matchFollowers(s, currentOffset+1)
                } else {
                    Pair(Optional.empty(), currentOffset)
                }
            }
            Representation.Same,  Representation.Str -> {
                val passedString = s[currentOffset]
                val thisName = when (repr.r) {
                    Representation.Same -> this.toString()
                    Representation.Str -> repr.get_str()
                    else -> {
                        throw Exception("Shouldn't reach: ${repr.r}")
                    }
                }
                val equality = thisName.equals(passedString, true)
                return if (equality) {
                    println("$passedString == $thisName, $this detected")
                    matchFollowers(s, currentOffset+1)
                } else {
                    println("none found.")
                    Pair(Optional.empty(), currentOffset)
                }
            }
        }
    }
    private fun matchFollowers(s: List<String>, currentOffset: Int): Pair<Optional<SqlTokens>, Int> {
        var mainres: Optional<SqlTokens> = Optional.of(this)
        this.childTokens = arrayListOf()
        var veryCurrentOffset = currentOffset
        for((followers, importance) in this.allowedFollowers) {
            val realFollowers = followers.map{ f -> Optional.ofNullable(f).getOrDefault(this) }
            val tokensRange = veryCurrentOffset until s.size
            println("    looking in ${s.slice(tokensRange)} for any of {${realFollowers.map { f -> f.toString() }} as follower for $this. Offset = $veryCurrentOffset")
            val fres = when(importance) {
                Importance.IsRequired -> false
                Importance.IsOptional -> true
            }

            val followersMatchResult = if (s.isNotEmpty()) {
                var r: Pair<Optional<SqlTokens>, Int> = Pair(Optional.empty(), veryCurrentOffset)
                for(f in realFollowers) {
                    val vco = veryCurrentOffset
                    val ri = f.matchString(s, veryCurrentOffset)
                    if (ri.first.isPresent) {
                        val child = ri.first.get()
                        if(child == ANY_STR) {
                            child.set(s[vco])
                        }
                        this.addChild(child)
                        r = ri
                        break
                    }
                }
                r
            } else {
                Pair(Optional.empty(), veryCurrentOffset)
            }
            veryCurrentOffset = followersMatchResult.second

            val keepDoing = mainres.isPresent && (followersMatchResult.first.isPresent || fres)
            if (!keepDoing) {
                mainres = Optional.empty()
                veryCurrentOffset = currentOffset
                break
            }
        }
        println("$this ${mainres} matched by followers. veryCurrentOffset = $veryCurrentOffset")
        return Pair(mainres, veryCurrentOffset)
    }
}


class tokenKeeper {

}

fun main(args: Array<String>) {
    println("Hello World!")
    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")

    val queryString = "SELECT name, date FROM tutorials_tbl WHERE name = 'Vasia' and date > 0";
    val splittedQuery = queryString.split("( |\n|((?=,))|((?<=,))|((?='))|((?<=')))".toRegex())
        .filter { ch -> ch.isNotEmpty() }
        .toList()
    println(splittedQuery.toString())
    val res = SqlTokens.SELECT.matchString(splittedQuery, 0)
    print("Result = $res")
    res.first.map { r -> r.printAsATree(0) }
}