import java.util.Optional
import java.util.stream.Stream

enum class Importance {
    IsRequired,
    IsOptional
}

enum class Representation {
    None,
    Word,
    CommonCharacter,
    Same,
    Str,
    Number,
}

val ANSI_CYAN ="\u001B[36m"
val ANSI_RED ="\u001B[31m"
val ANSI_PURPLE = "\u001B[35m"
val ANSI_GREEN ="\u001B[32m"
val ANSI_RESET ="\u001B[0m"


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
        fun Word() = Representator(Representation.Word, null)
        fun Number() = Representator(Representation.Number, null)
        fun Same() = Representator(Representation.Same, null)
        fun CommonCharacter() = Representator(Representation.CommonCharacter, null)
        fun Str(s: String) = Representator(Representation.Str, s)
    }
}

enum class SqlToken(public val repr: Representator) {
    GROUP(Representator.Same()),
    ORDER(Representator.Same()),
    WITH(Representator.Same()),
    BY(Representator.Same()),
    ON(Representator.Same()),
    INNER(Representator.Same()),
    LEFT(Representator.Same()),
    RIGHT(Representator.Same()),
    JOIN(Representator.Same()),
    JOIN_EXPR_SEQ(Representator.None()),
    POSITION(Representator.Number()),
    COMMA(Representator.Str(",")),
    ESCAPE(Representator.Str("""\""")),
    ESCAPED_SPECIAL(Representator.None()),
    QUOTE(Representator.Str("'")),
    POINT(Representator.Str(".")),
    COMMON_CHARACTER(Representator.CommonCharacter()),
    COMMON_STRING(Representator.None()),
    COUNT(Representator.Same()),
    MAX(Representator.Same()),
    MIN(Representator.Same()),
    SUM(Representator.Same()),
    AVG(Representator.Same()),
    SCOPED_COL_NAME(Representator.None()),
    FUNCTION_CALL(Representator.None()),
    COL_NAME(Representator.Word()),
    COL_NAME_EXPR_SELECT(Representator.None()),
    COL_NAME_NEXT_SELECT(Representator.None()),
    COL_NAME_SEQ_SELECT(Representator.None()),
    ITEM_TO_SELECT(Representator.None()),
    COL_NAME_EXPR(Representator.None()),
    COL_NAME_NEXT(Representator.None()),
    COL_NAME_SEQ(Representator.None()),
    TABLE_NAME_PREFIX(Representator.None()),
    TABLE_NAME(Representator.Word()),
    TABLE_NAME_EXPR(Representator.None()),
    SELECT_SOURCE_NEXT(Representator.None()),
    SELECT_SOURCE_SEQ(Representator.None()),
    ANY_WORD(Representator.Word()),
    POSITION_NEXT(Representator.None()),
    POSITION_SEQ(Representator.None()),
    QUOTED_STR(Representator.None()),
    WILDCARD(Representator.Str("*")),
    EQUAL(Representator.Str("=")),
    LESS(Representator.Str("<")),
    LESS_EQ(Representator.Str("<=")),
    MORE(Representator.Str(">")),
    MORE_EQ(Representator.Str(">=")),
    AND(Representator.Same()),
    OR(Representator.Same()),
    EXPR_PART(Representator.None()),
    SCOPED_EXPR(Representator.None()),
    BASE_EXPR(Representator.None()),
    EXPR(Representator.None()),
    EXPR_NEXT(Representator.None()),
    EXPR_SEQ(Representator.None()),
    CALC_EXPR(Representator.None()),
    OFFSET(Representator.Same()),
    SC_LEFT(Representator.Str("(")),
    SC_RIGHT(Representator.Str(")")),
    DESC(Representator.Same()),
    ASC(Representator.Same()),
    AS_EXPR(Representator.None()),
    AS(Representator.Same()),
    NUMBER(Representator.Number()),
    CHAR_SET_EXPR(Representator.Str("CHARACTER SET")),
    INTO_OUTFILE(Representator.Str("INTO OUTFILE")),
    VARNAME_EXPR(Representator.None()),
    INTO_DUMPFILE(Representator.Str("INTO DUMPFILE")),
    WITH_ROLLUP(Representator.Str("WITH ROLLUP")),
    INTO_VARNAME(Representator.Str("INTO")),
    INTO_OPTION(Representator.None()),
    RC_OFFSET_OPTVAL(
        Representator.None()
    ),
    RC_OFFSET_RQ(Representator.None()),
    RC_OFFSET_OPT(
        Representator.None()
    ),
    UPDATE(Representator.Same()),
    SHARE(Representator.Same()),
    NOWAIT(Representator.Same()),
    LOCK_SHARED_MODE(Representator.Str("LOCK IN SHARE MODE")),
    SKIP_LOCKED(Representator.Str("SKIP LOCKED")),
    OF(Representator.Same()),//
    FOR(Representator.Same()),
    LIMIT_EXPR(Representator.None()),
    LIMIT(Representator.Same()),
    GROUP_BY_EXPR(
        Representator.None()
    ),
    GROUP_BY(
        Representator.Str("GROUP BY")
    ),
    WHERE_CONDITION(Representator.None()),
    ORDER_BY_EXPR(
        Representator.None()
    ),
    ORDER_BY(
        Representator.Str("ORDER BY")
    ),
    WINDOW_EXPR(
        Representator.None()
    ),
    WINDOW(
        Representator.Same()
    ),
    HAVING(Representator.Same()),
    WHERE(Representator.Same()),
    SUB_SELECT(Representator.None()),
    PARTITION_LIST(Representator.Word()),
    PARTITION(Representator.Same()),
    FROM(Representator.Same()),
    SQL_CALC_FOUND_ROWS(Representator.Same()),
    SQL_NO_CACHE(Representator.Same()),
    SQL_BUFFER_RESULT(Representator.Same()),
    SQL_BIG_RESULT(Representator.Same()),
    SQL_SMALL_RESULT(Representator.Same()),
    HIGH_PRIORITY(Representator.Same()),
    DISTINCTROW(Representator.Same()),
    DISTINCT(Representator.Same()),
    ALL(Representator.Same()),
    SELECT(
        Representator.Same()
    ),

}
val allowedFollowers: Map<SqlToken, Array<Pair<Array<SqlToken>, Importance>>> = mapOf(
    SqlToken.ESCAPED_SPECIAL to
            arrayOf(
            Pair(arrayOf(SqlToken.ESCAPE), Importance.IsRequired),
            Pair(arrayOf(SqlToken.QUOTE, SqlToken.ESCAPE), Importance.IsRequired),
        ),
    SqlToken.COMMON_STRING to
            arrayOf(
            Pair(arrayOf(SqlToken.ESCAPED_SPECIAL, SqlToken.COMMON_CHARACTER), Importance.IsRequired),
            Pair(arrayOf(SqlToken.COMMON_STRING), Importance.IsOptional),
        ),
    SqlToken.FUNCTION_CALL to
            arrayOf(
            Pair(arrayOf(SqlToken.ANY_WORD), Importance.IsRequired),
            Pair(arrayOf(SqlToken.SC_LEFT), Importance.IsRequired),
            Pair(arrayOf(SqlToken.COL_NAME_SEQ_SELECT), Importance.IsRequired),
            Pair(arrayOf(SqlToken.SC_RIGHT), Importance.IsRequired),
        ),
    SqlToken.TABLE_NAME_PREFIX to
            arrayOf(
            Pair(arrayOf(SqlToken.TABLE_NAME), Importance.IsRequired),
            Pair(arrayOf(SqlToken.POINT), Importance.IsRequired),
        ),
    SqlToken.ITEM_TO_SELECT to
            arrayOf(
            Pair(arrayOf(SqlToken.EXPR_PART, SqlToken.COL_NAME_EXPR_SELECT), Importance.IsRequired),
            Pair(arrayOf(SqlToken.AS_EXPR), Importance.IsOptional),
        ),
    SqlToken.COL_NAME_EXPR to
            arrayOf(
            Pair(arrayOf(SqlToken.TABLE_NAME_PREFIX), Importance.IsOptional),
            Pair(arrayOf(SqlToken.COL_NAME), Importance.IsRequired),
        ),
    SqlToken.COL_NAME_EXPR_SELECT to
                arrayOf(
                Pair(arrayOf(SqlToken.TABLE_NAME_PREFIX), Importance.IsOptional),
                Pair(arrayOf(SqlToken.COL_NAME, SqlToken.WILDCARD), Importance.IsRequired),
            ),
    SqlToken.COL_NAME_NEXT to
            arrayOf(
            Pair(arrayOf(SqlToken.COMMA), Importance.IsRequired),
            Pair(arrayOf(SqlToken.EXPR_PART, SqlToken.COL_NAME_EXPR), Importance.IsRequired),
            Pair(arrayOf(SqlToken.COL_NAME_NEXT), Importance.IsOptional),
        ),
    SqlToken.COL_NAME_SEQ to
            arrayOf(
            Pair(arrayOf(SqlToken.EXPR_PART, SqlToken.COL_NAME_EXPR), Importance.IsRequired),
            Pair(arrayOf(SqlToken.COL_NAME_NEXT), Importance.IsOptional),
        ),
    SqlToken.COL_NAME_NEXT_SELECT to
            arrayOf(
            Pair(arrayOf(SqlToken.COMMA), Importance.IsRequired),
            Pair(arrayOf(SqlToken.ITEM_TO_SELECT), Importance.IsRequired),
            Pair(arrayOf(SqlToken.COL_NAME_NEXT_SELECT), Importance.IsOptional),
        ),
    SqlToken.COL_NAME_SEQ_SELECT to
            arrayOf(
            Pair(arrayOf(SqlToken.ITEM_TO_SELECT), Importance.IsRequired),
            Pair(arrayOf(SqlToken.COL_NAME_NEXT_SELECT), Importance.IsOptional),
        ),
    SqlToken.TABLE_NAME_EXPR to
            arrayOf(
            Pair(arrayOf(SqlToken.TABLE_NAME), Importance.IsRequired),
            Pair(arrayOf(SqlToken.TABLE_NAME), Importance.IsOptional),
        ),
    SqlToken.SELECT_SOURCE_NEXT to
            arrayOf(
            Pair(arrayOf(SqlToken.COMMA), Importance.IsRequired),
            Pair(arrayOf(SqlToken.TABLE_NAME_EXPR, SqlToken.SUB_SELECT), Importance.IsRequired),
            Pair(arrayOf(SqlToken.SELECT_SOURCE_NEXT), Importance.IsOptional),
        ),
    SqlToken.SELECT_SOURCE_SEQ to
            arrayOf(
            Pair(arrayOf(SqlToken.TABLE_NAME_EXPR, SqlToken.SUB_SELECT), Importance.IsRequired),
            Pair(arrayOf(SqlToken.SELECT_SOURCE_NEXT), Importance.IsOptional),
        ),
    SqlToken.POSITION_NEXT to
            arrayOf(
            Pair(arrayOf(SqlToken.COMMA), Importance.IsRequired),
            Pair(arrayOf(SqlToken.POSITION), Importance.IsRequired),
            Pair(arrayOf(SqlToken.POSITION_NEXT), Importance.IsOptional),
        ),
    SqlToken.POSITION_SEQ to
            arrayOf(
            Pair(arrayOf(SqlToken.POSITION), Importance.IsRequired),
            Pair(arrayOf(SqlToken.POSITION_NEXT), Importance.IsOptional),
        ),
    SqlToken.QUOTED_STR to
            arrayOf(
            Pair(arrayOf(SqlToken.QUOTE), Importance.IsRequired),
            Pair(arrayOf(SqlToken.COMMON_STRING), Importance.IsRequired),
            Pair(arrayOf(SqlToken.QUOTE), Importance.IsRequired),
        ),
    SqlToken.EXPR_PART to
            arrayOf(
                Pair(arrayOf(SqlToken.NUMBER, SqlToken.CALC_EXPR, SqlToken.QUOTED_STR), Importance.IsRequired),
                ),
    SqlToken.SCOPED_EXPR to
            arrayOf(
            Pair(arrayOf(SqlToken.SC_LEFT), Importance.IsRequired),
            Pair(arrayOf(SqlToken.BASE_EXPR), Importance.IsRequired),
            Pair(arrayOf(SqlToken.SC_RIGHT), Importance.IsRequired),
        ),
    SqlToken.BASE_EXPR to
            arrayOf(
            Pair(arrayOf(SqlToken.EXPR_PART, SqlToken.COL_NAME_EXPR), Importance.IsRequired),
            Pair(arrayOf(SqlToken.MORE_EQ,SqlToken.LESS_EQ,SqlToken.MORE,SqlToken.LESS,SqlToken.EQUAL), Importance.IsRequired),
            Pair(arrayOf(SqlToken.EXPR_PART, SqlToken.COL_NAME_EXPR), Importance.IsRequired),
        ),
    SqlToken.EXPR to
            arrayOf(
            Pair(arrayOf(SqlToken.BASE_EXPR, SqlToken.SCOPED_EXPR), Importance.IsRequired),
        ),
    SqlToken.JOIN_EXPR_SEQ to
            arrayOf(
            Pair(arrayOf(SqlToken.LEFT, SqlToken.RIGHT, SqlToken.INNER), Importance.IsRequired),
            Pair(arrayOf(SqlToken.JOIN), Importance.IsRequired),
            Pair(arrayOf(SqlToken.TABLE_NAME_EXPR), Importance.IsRequired),
            Pair(arrayOf(SqlToken.ON), Importance.IsRequired),
            Pair(arrayOf(SqlToken.EXPR), Importance.IsRequired),
            Pair(arrayOf(SqlToken.JOIN_EXPR_SEQ), Importance.IsOptional),
        ),
    SqlToken.AS_EXPR to
            arrayOf(
            Pair(arrayOf(SqlToken.AS), Importance.IsRequired),
            Pair(arrayOf(SqlToken.ANY_WORD), Importance.IsRequired),
        ),
    SqlToken.EXPR_NEXT to
            arrayOf(
            Pair(arrayOf(SqlToken.AND,SqlToken.OR), Importance.IsRequired),
            Pair(arrayOf(SqlToken.EXPR), Importance.IsRequired),
            Pair(arrayOf(SqlToken.EXPR_NEXT), Importance.IsOptional),
        ),
    SqlToken.EXPR_SEQ to
            arrayOf(
            Pair(arrayOf(SqlToken.EXPR), Importance.IsRequired),
            Pair(arrayOf(SqlToken.EXPR_NEXT), Importance.IsOptional),
        ),
    SqlToken.CHAR_SET_EXPR to
            arrayOf(
            Pair(arrayOf(SqlToken.ANY_WORD), Importance.IsRequired),
        ),
    SqlToken.INTO_OUTFILE to
        arrayOf(
            Pair(arrayOf(SqlToken.QUOTED_STR), Importance.IsRequired),
            Pair(arrayOf(SqlToken.CHAR_SET_EXPR), Importance.IsOptional),
            Pair(arrayOf(SqlToken.ANY_WORD), Importance.IsRequired),
        ),
    SqlToken.VARNAME_EXPR to
            arrayOf(
            Pair(arrayOf(SqlToken.ANY_WORD), Importance.IsRequired),
            Pair(arrayOf(SqlToken.VARNAME_EXPR), Importance.IsOptional),
        ),
    SqlToken.INTO_DUMPFILE to arrayOf(Pair(arrayOf(SqlToken.QUOTED_STR), Importance.IsRequired),),
    SqlToken.INTO_VARNAME to arrayOf(Pair(arrayOf(SqlToken.VARNAME_EXPR), Importance.IsRequired),),
    SqlToken.INTO_OPTION to arrayOf(Pair(arrayOf(SqlToken.INTO_OUTFILE, SqlToken.INTO_DUMPFILE, SqlToken.INTO_VARNAME), Importance.IsRequired)),
    SqlToken.RC_OFFSET_OPTVAL to
            arrayOf(
            Pair(arrayOf(SqlToken.NUMBER), Importance.IsRequired),
            Pair(arrayOf(SqlToken.COMMA), Importance.IsRequired),
            ),
    SqlToken.RC_OFFSET_RQ to
            arrayOf(
            Pair(arrayOf(SqlToken.NUMBER), Importance.IsRequired),
            Pair(arrayOf(SqlToken.OFFSET), Importance.IsRequired),
            Pair(arrayOf(SqlToken.NUMBER), Importance.IsRequired),
            ),
    SqlToken.RC_OFFSET_OPT to
            arrayOf(
            Pair(arrayOf(SqlToken.RC_OFFSET_OPTVAL), Importance.IsOptional),
            Pair(arrayOf(SqlToken.NUMBER), Importance.IsRequired),
            ),
    SqlToken.OF to arrayOf(Pair(arrayOf(SqlToken.TABLE_NAME), Importance.IsRequired)),
    SqlToken.LESS to arrayOf(Pair(arrayOf(SqlToken.EQUAL), Importance.IsOptional)),
    SqlToken.MORE to arrayOf(Pair(arrayOf(SqlToken.EQUAL), Importance.IsOptional)),
    SqlToken.FOR to
            arrayOf(
            Pair(arrayOf(SqlToken.UPDATE, SqlToken.SHARE), Importance.IsRequired),
            Pair(arrayOf(SqlToken.OF), Importance.IsOptional),
            Pair(arrayOf(SqlToken.NOWAIT, SqlToken.SKIP_LOCKED), Importance.IsOptional),
            Pair(arrayOf(SqlToken.LOCK_SHARED_MODE), Importance.IsOptional),
        ),
    SqlToken.LIMIT_EXPR to
            arrayOf(
            Pair(arrayOf(SqlToken.RC_OFFSET_RQ, SqlToken.RC_OFFSET_OPT), Importance.IsRequired),
        ),
    SqlToken.LIMIT to arrayOf(Pair(arrayOf(SqlToken.LIMIT_EXPR), Importance.IsRequired),),
    SqlToken.GROUP_BY_EXPR to
            arrayOf(
            Pair(arrayOf(SqlToken.COL_NAME_SEQ, SqlToken.POSITION_SEQ), Importance.IsRequired),
        ),
    SqlToken.GROUP_BY to
            arrayOf(
            Pair(arrayOf(SqlToken.GROUP_BY_EXPR), Importance.IsRequired),
            Pair(arrayOf(SqlToken.WITH_ROLLUP), Importance.IsOptional)),
    SqlToken.WHERE_CONDITION to arrayOf(Pair(arrayOf(SqlToken.EXPR_SEQ), Importance.IsRequired)),
    SqlToken.SCOPED_COL_NAME to
            arrayOf(
                Pair(arrayOf(SqlToken.SC_LEFT), Importance.IsRequired),
                Pair(arrayOf(SqlToken.COL_NAME_EXPR), Importance.IsRequired),
                Pair(arrayOf(SqlToken.SC_RIGHT), Importance.IsRequired),
            ),
    SqlToken.CALC_EXPR to
            arrayOf(
                Pair(arrayOf(SqlToken.COUNT,SqlToken.MAX,SqlToken.MIN,SqlToken.SUM,SqlToken.AVG, SqlToken.FUNCTION_CALL), Importance.IsRequired),
            ),
    SqlToken.COUNT to
            arrayOf(
                Pair(arrayOf(SqlToken.SC_LEFT), Importance.IsRequired),
                Pair(arrayOf(SqlToken.COL_NAME_EXPR, SqlToken.WILDCARD), Importance.IsRequired),
                Pair(arrayOf(SqlToken.SC_RIGHT), Importance.IsRequired),
            ),
    SqlToken.MAX to arrayOf(Pair(arrayOf(SqlToken.SCOPED_COL_NAME), Importance.IsRequired),),
    SqlToken.MIN to arrayOf(Pair(arrayOf(SqlToken.SCOPED_COL_NAME), Importance.IsRequired),),
    SqlToken.SUM to arrayOf(Pair(arrayOf(SqlToken.SCOPED_COL_NAME), Importance.IsRequired),),
    SqlToken.AVG to arrayOf(Pair(arrayOf(SqlToken.SCOPED_COL_NAME), Importance.IsRequired),),
    SqlToken.ORDER_BY_EXPR to
            arrayOf(
            Pair(arrayOf(SqlToken.COL_NAME, SqlToken.CALC_EXPR, SqlToken.POSITION), Importance.IsRequired),
            Pair(arrayOf(SqlToken.ASC, SqlToken.DESC), Importance.IsOptional),
            Pair(arrayOf(SqlToken.ORDER_BY_EXPR), Importance.IsOptional),
        ),
    SqlToken.ORDER_BY to
            arrayOf(
            Pair(arrayOf(SqlToken.ORDER_BY_EXPR), Importance.IsRequired),
            Pair(arrayOf(SqlToken.WITH_ROLLUP), Importance.IsOptional),
        ),
    SqlToken.WINDOW_EXPR to
            arrayOf(
            Pair(arrayOf(SqlToken.ANY_WORD), Importance.IsRequired),
            Pair(arrayOf(SqlToken.AS), Importance.IsRequired),
            Pair(arrayOf(SqlToken.SC_LEFT), Importance.IsRequired),
            Pair(arrayOf(SqlToken.ANY_WORD), Importance.IsRequired),
            Pair(arrayOf(SqlToken.SC_RIGHT), Importance.IsRequired),
            Pair(arrayOf(SqlToken.WINDOW_EXPR), Importance.IsOptional),
            ),
    SqlToken.WINDOW to
            arrayOf(
            Pair(arrayOf(SqlToken.WINDOW_EXPR), Importance.IsRequired),
            ),
    SqlToken.HAVING to arrayOf(Pair(arrayOf(SqlToken.WHERE_CONDITION), Importance.IsRequired)),
    SqlToken.WHERE to arrayOf(Pair(arrayOf(SqlToken.WHERE_CONDITION), Importance.IsRequired)),
    SqlToken.SUB_SELECT to
            arrayOf(
            Pair(arrayOf(SqlToken.SC_LEFT), Importance.IsRequired),
            Pair(arrayOf(SqlToken.SELECT), Importance.IsRequired),  //Going deeper at this very point
            Pair(arrayOf(SqlToken.SC_RIGHT), Importance.IsRequired),
            Pair(arrayOf(SqlToken.ANY_WORD), Importance.IsRequired),
        ),
    SqlToken.PARTITION_LIST to arrayOf(),
    SqlToken.PARTITION to arrayOf(Pair(arrayOf(SqlToken.PARTITION_LIST), Importance.IsRequired)),
    SqlToken.FROM to
            arrayOf(
            Pair(arrayOf(SqlToken.SELECT_SOURCE_SEQ, SqlToken.SUB_SELECT), Importance.IsRequired),
            Pair(arrayOf(SqlToken.JOIN_EXPR_SEQ), Importance.IsOptional),
            Pair(arrayOf(SqlToken.PARTITION), Importance.IsOptional)
        ),
    SqlToken.SELECT to
            arrayOf(
            Pair(arrayOf(SqlToken.ALL, SqlToken.DISTINCT, SqlToken.DISTINCTROW), Importance.IsOptional),
            Pair(arrayOf(SqlToken.HIGH_PRIORITY), Importance.IsOptional),
            Pair(arrayOf(SqlToken.SQL_SMALL_RESULT), Importance.IsOptional),
            Pair(arrayOf(SqlToken.SQL_BIG_RESULT), Importance.IsOptional),
            Pair(arrayOf(SqlToken.SQL_BUFFER_RESULT), Importance.IsOptional),
            Pair(arrayOf(SqlToken.SQL_NO_CACHE), Importance.IsOptional),
            Pair(arrayOf(SqlToken.SQL_CALC_FOUND_ROWS), Importance.IsOptional),
            Pair(arrayOf(SqlToken.COL_NAME_SEQ_SELECT, SqlToken.WILDCARD), Importance.IsRequired),
            Pair(arrayOf(SqlToken.INTO_OPTION), Importance.IsOptional),
            Pair(arrayOf(SqlToken.FROM), Importance.IsOptional),
            Pair(arrayOf(SqlToken.WHERE), Importance.IsOptional),
            Pair(arrayOf(SqlToken.GROUP_BY), Importance.IsOptional),
            Pair(arrayOf(SqlToken.HAVING), Importance.IsOptional),
            Pair(arrayOf(SqlToken.WINDOW), Importance.IsOptional),
            Pair(arrayOf(SqlToken.ORDER_BY), Importance.IsOptional),
            Pair(arrayOf(SqlToken.LIMIT), Importance.IsOptional),
            Pair(arrayOf(SqlToken.INTO_OPTION), Importance.IsOptional),
            Pair(arrayOf(SqlToken.FOR), Importance.IsOptional),
            Pair(arrayOf(SqlToken.INTO_OPTION), Importance.IsOptional),)
)

fun checkWordIsASpecialSqlToken(word: String): Boolean {
    return SqlToken.values().any {t ->
        when(t.repr.r){
            Representation.Str -> t.repr.get_str().equals(word, true)
            Representation.Same -> t.toString().equals(word, true)
            else -> {
                false
            }
        }
    }
}

class TokenKeeper(val token: SqlToken) {
    private lateinit var children: MutableList<TokenKeeper>
    var stringBody: Optional<String> = Optional.empty()
    fun set(s: String) {
        println("Set ${this.token} to '$s'")
        stringBody = Optional.of(s)
    }
    fun get(): String {return stringBody.get()}
    fun hasBody(): Boolean {return stringBody.isPresent}

    fun addChild(t: TokenKeeper) {
        children.add(t)
    }
    fun getChildren(): Stream<TokenKeeper> {
        return children.stream()
    }
    private fun toPrettyString(): String {
        return when (this.token.repr.r) {
                Representation.None -> "--${this.token}${if(this.hasBody()) ": ${this.get()}" else "" }"
                Representation.Word -> "${this.token}: ${this.get()}"
                Representation.Number -> "${this.token}: ${this.get()}"
                Representation.Same -> token.toString()
                Representation.Str -> token.repr.get_str()
                Representation.CommonCharacter -> "${this.token}: ${this.get()}"
            }

    }
    private fun toSimpleString(): String {
        return if (this.hasBody()) {
            this.get()
        } else {
            when (this.token.repr.r) {
                Representation.None -> ""
                else -> this.toPrettyString()
            }
        }
    }
    fun printAsATree(offset: Int){
        println(" ".repeat(offset) + this.toPrettyString())
        for (c in this.children) {
            c.printAsATree(offset + 2)
        }
    }

    private fun mustSkipThisSymbol(c: Char): Boolean {
        return (c == ' '
                && !arrayOf(Representation.CommonCharacter, Representation.None).contains(this.token.repr.r)
                && this.token != SqlToken.ESCAPE)
    }

    fun matchString(s: String, currentOffset: Int): Pair<Optional<TokenKeeper>, Int> {
        val tokensRange = currentOffset until s.length
        print("${ANSI_CYAN}Try to find ${this.token} in '${s.substring(tokensRange)}'... ${ANSI_RESET}")
        if(s.length <= currentOffset) return Pair(Optional.empty(), currentOffset)
        if(this.mustSkipThisSymbol(s[currentOffset]))
            return matchString(s, currentOffset+1) //Token can't start with space
        var substring: String
        return when(token.repr.r) {
            Representation.None -> {
                println("going further with 'None' as $token.")
                this.matchFollowers(s, currentOffset)
            }
            Representation.CommonCharacter -> {
                val currentCharacter = s[currentOffset]
                return if(!"""\'""".contains(currentCharacter)){
                    println("'$currentCharacter' detected as ${this.token}")
                    this.set(currentCharacter.toString())
                    this.matchFollowers(s, currentOffset+1)
                } else {
                    Pair(Optional.empty(), currentOffset)
                }
            }
            Representation.Word, Representation.Number -> {

                val expression = when(token.repr.r) {
                    Representation.Number -> "\\d+?".toRegex()
                    Representation.Word -> "^(?!\\d|\\)|\\()\\w*?$".toRegex() //Col name can't start with digit
                    else -> {
                        throw Exception("Shouldn't be reached")
                    }
                }

                var nextOffset = currentOffset+1
                substring = s.substring(currentOffset until nextOffset)
                val resInitial = substring.matches(expression)
                var res = resInitial
                while (nextOffset<s.length && res) {
                    nextOffset += 1
                    substring = s.substring(currentOffset until nextOffset)
                    res = substring.matches(expression)
                }
                val resEnding = if(resInitial) {
                    if (!res) {
                        nextOffset -= 1
                        substring = substring.substring(0 until substring.length - 1)
                    }
                    val isASpecialWord = checkWordIsASpecialSqlToken(substring)
                    if(isASpecialWord) println("${ANSI_RED}'$substring' is a special word and can't be matched as ${this.token}${ANSI_RESET}")
                    !isASpecialWord
                } else {
                    false
                }
                return if(resEnding) {
                    println("${ANSI_GREEN}$substring detected as ${this.token}${ANSI_RESET}")
                    this.set(substring)
                    this.matchFollowers(s, nextOffset)
                } else {
                    Pair(Optional.empty(), currentOffset)
                }
            }
            Representation.Same,  Representation.Str -> {
                val thisName = when (token.repr.r) {
                    Representation.Same -> this.token.toString()
                    Representation.Str -> token.repr.get_str()
                    else -> {
                        throw Exception("Shouldn't reach: ${token.repr.r}")
                    }
                }
                val endBound = Math.min(currentOffset + thisName.length, s.length)
                val passedString = s.substring(currentOffset until endBound)
                val equality = thisName.equals(passedString, true)
                return if (equality) {
                    println("${ANSI_GREEN}$passedString == $thisName, ${this.token} detected${ANSI_RESET}")
                    matchFollowers(s, currentOffset+thisName.length)
                } else {
                    println("none found.")
                    Pair(Optional.empty(), currentOffset)
                }
            }
        }
    }
    private fun matchFollowers(s: String, currentOffset: Int): Pair<Optional<TokenKeeper>, Int> {
        var mainres: Optional<TokenKeeper> = Optional.of(this)
        this.children = arrayListOf()
        var veryCurrentOffset = currentOffset
        val thisFollowers = Optional.ofNullable(allowedFollowers[this.token]).orElse(arrayOf())
        for((followers, importance) in thisFollowers) {

            val tokensRange = veryCurrentOffset until s.length
            println("${ANSI_PURPLE}looking in '${s.substring(tokensRange)}' for any of ${followers.map { f -> f.toString() }} as follower for ${this.token}. Offset = ${veryCurrentOffset}${ANSI_RESET}")
            val fres = when(importance) {
                Importance.IsRequired -> false
                Importance.IsOptional -> true
            }

            val followersMatchResult = if (s.isNotEmpty()) {
                var r: Pair<Optional<TokenKeeper>, Int> = Pair(Optional.empty(), veryCurrentOffset)

                val matchedFollowers = followers.map { f ->
                    TokenKeeper(f).matchString(s, veryCurrentOffset)
                }.filter { mayBeFollower -> mayBeFollower.first.isPresent }

                if (matchedFollowers.isNotEmpty()) {
                    val bestFollower = matchedFollowers.maxBy { matchedFollower -> matchedFollower.second }
                    val child = bestFollower.first.get()
                    val sz = matchedFollowers.size
                    if(sz>1) println("${ANSI_RED}WARNING! FOUND $sz possible followers for ${this.token}: ${matchedFollowers.map { ft -> ft.first.get().token.toString() }}.${ANSI_RESET}" +
                                     " ${ANSI_GREEN}Best possible follower is ${child.token}${ANSI_RESET}")
                    this.addChild(child)
                    r = bestFollower
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
            if (veryCurrentOffset==s.length) {
                break
            }
        }

        if(this.token == SqlToken.QUOTED_STR && mainres.isPresent) {
            var r: ArrayList<String> = arrayListOf()
            this.joinStrTokens(r)
            this.set(r.joinToString(separator = ""))
            this.children = listOf<TokenKeeper>().toMutableList()
        }

        val (color, word) = if (mainres.isPresent) Pair(ANSI_GREEN, "") else Pair(ANSI_RED, "not")

        println("${color}${this.token} ${word} matched by followers. veryCurrentOffset = ${veryCurrentOffset}${ANSI_RESET}")
        return Pair(mainres, veryCurrentOffset)
    }

    private fun joinStrTokens(result: ArrayList<String>) {
        val currentStr = this.toSimpleString()
        result.add(currentStr)
        for(child in this.children) {
            child.joinStrTokens(result)
        }
    }
}

fun main(args: Array<String>) {
    //val queryString = "SELECT name, date FROM tutorials_tbl WHERE name = 'Vasia' and date > 0";
    val queryString = args.getOrElse(0
    ) { _: Int -> """SELECT nm, dt FROM ( SELECT * FROM ( SELECT name AS nm, date AS dt FROM tutorials_tbl WHERE nm = 'Vasia \'' ORDER BY 2)qx WHERE date > 0 )qy WHERE name = 'Vasia\\' and date > 0 GROUP BY date HAVING date>400""" }
    val res = TokenKeeper(SqlToken.SELECT).matchString(queryString, 0)
    if (res.second < queryString.length){
        throw Exception("Can't match string $queryString. Matched ${res.second} of ${queryString.length} possible chars")
    }
    if (res.first.isEmpty){
        throw Exception("Can't match string $queryString as an SQL query")
    }
    println("Result = $res")
    println("Query tree:")
    res.first.map { r -> r.printAsATree(0) }
}