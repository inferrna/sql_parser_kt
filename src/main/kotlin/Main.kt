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
        fun Word() = Representator(Representation.Word, null)
        fun Same() = Representator(Representation.Same, null)
        fun CommonCharacter() = Representator(Representation.CommonCharacter, null)
        fun Str(s: String) = Representator(Representation.Str, s)
    }
}

enum class SqlToken(public val repr: Representator) {
    WITH_ROLLUP(Representator.Str("WITH ROLLUP")),
    POSITION(Representator.None()),
    COMMA(Representator.Str(",")),
    ESCAPE(Representator.Str("""\""")),
    ESCAPED_SPECIAL(Representator.None()),
    QUOTE(Representator.Str("'")),
    COMMON_CHARACTER(Representator.CommonCharacter()),
    COMMON_STRING(Representator.None()),
    ANY_WORD(Representator.Word()),
    ANY_STR_NEXT(Representator.None()),
    ANY_STR_SEQ(Representator.None()),
    QUOTED_STR(Representator.None()),
    WILDCARD(Representator.Str("*")),
    EQUAL(Representator.Str("=")),
    LESS(Representator.Str("<")),
    MORE(Representator.Str(">")),
    AND(Representator.Same()),
    OR(Representator.Same()),
    EXPR(Representator.None()),
    EXPR_NEXT(Representator.None()),
    EXPR_SEQ(Representator.None()),
    OFFSET(Representator.Same()),
    SC_LEFT(Representator.Str("(")),
    SC_RIGHT(Representator.Str(")")),
    DESC(Representator.Same()),
    ASC(Representator.Same()),
    AS(Representator.Same()),
    NUMBER(Representator.None()),
    COL_NAME(Representator.Word()),
    CHAR_SET_EXPR(Representator.Str("CHARACTER SET")),
    INTO_OUTFILE(Representator.Str("INTO OUTFILE")),
    VARNAME_EXPR(Representator.None()),
    INTO_DUMPFILE(Representator.Str("INTO DUMPFILE")),
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
    COUNT(Representator.Same()),
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
    )
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
    SqlToken.ANY_STR_NEXT to
            arrayOf(
            Pair(arrayOf(SqlToken.COMMA), Importance.IsRequired),
            Pair(arrayOf(SqlToken.ANY_WORD), Importance.IsRequired),
            Pair(arrayOf(SqlToken.ANY_STR_NEXT), Importance.IsOptional),
        ),
    SqlToken.ANY_STR_SEQ to
            arrayOf(
            Pair(arrayOf(SqlToken.ANY_WORD), Importance.IsRequired),
            Pair(arrayOf(SqlToken.ANY_STR_NEXT), Importance.IsOptional),
        ),
    SqlToken.QUOTED_STR to
            arrayOf(
            Pair(arrayOf(SqlToken.QUOTE), Importance.IsRequired),
            Pair(arrayOf(SqlToken.COMMON_STRING), Importance.IsRequired),
            Pair(arrayOf(SqlToken.QUOTE), Importance.IsRequired),
        ),
    SqlToken.EXPR to
            arrayOf(
            Pair(arrayOf(SqlToken.ANY_WORD, SqlToken.QUOTED_STR), Importance.IsRequired),
            Pair(arrayOf(SqlToken.MORE,SqlToken.LESS,SqlToken.EQUAL), Importance.IsRequired),
            Pair(arrayOf(SqlToken.ANY_WORD, SqlToken.QUOTED_STR), Importance.IsRequired),
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
    SqlToken.OF to arrayOf(Pair(arrayOf(SqlToken.ANY_STR_SEQ), Importance.IsRequired)),
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
            Pair(arrayOf(SqlToken.ANY_WORD, SqlToken.EXPR_SEQ, SqlToken.POSITION), Importance.IsRequired),
            Pair(arrayOf(SqlToken.GROUP_BY_EXPR), Importance.IsOptional),
        ),
    SqlToken.GROUP_BY to
            arrayOf(
            Pair(arrayOf(SqlToken.GROUP_BY_EXPR), Importance.IsRequired),
            Pair(arrayOf(SqlToken.WITH_ROLLUP), Importance.IsOptional)),
    SqlToken.WHERE_CONDITION to arrayOf(Pair(arrayOf(SqlToken.EXPR_SEQ), Importance.IsRequired)),
    SqlToken.COUNT to
            arrayOf(
            Pair(arrayOf(SqlToken.SC_LEFT), Importance.IsRequired),
            Pair(arrayOf(SqlToken.ANY_WORD), Importance.IsRequired),
            Pair(arrayOf(SqlToken.SC_RIGHT), Importance.IsRequired),
        ),
    SqlToken.ORDER_BY_EXPR to
            arrayOf(
            Pair(arrayOf(SqlToken.COL_NAME, SqlToken.COUNT, SqlToken.POSITION), Importance.IsRequired),
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
        ),
    SqlToken.PARTITION_LIST to arrayOf(),
    SqlToken.PARTITION to arrayOf(Pair(arrayOf(SqlToken.PARTITION_LIST), Importance.IsRequired)),
    SqlToken.FROM to
            arrayOf(
            Pair(arrayOf(SqlToken.ANY_STR_SEQ, SqlToken.SUB_SELECT), Importance.IsRequired),
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
            Pair(arrayOf(SqlToken.ANY_STR_SEQ, SqlToken.WILDCARD), Importance.IsRequired),
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

class TokenKeeper(val token: SqlToken) {
    private lateinit var children: MutableList<TokenKeeper>
    private lateinit var stringBody: String
    fun set(s: String) {
        println("Set ${this.token} to $s")
        stringBody = s
    }
    fun get(): String {return stringBody}

    fun addChild(t: TokenKeeper) {
        children.add(t)
    }
    fun getChildren(): Stream<TokenKeeper> {
        return children.stream()
    }
    private fun toPrettyString(): String {
        return when (this.token.repr.r) {
            Representation.None -> "--${this.token}"
            Representation.Word -> this.get()
            Representation.Same -> token.toString()
            Representation.Str -> token.repr.get_str()
            Representation.CommonCharacter -> this.get()
        }
    }
    fun printAsATree(offset: Int){
        println(" ".repeat(offset) + this.toPrettyString())
        for (c in this.children) {
            c.printAsATree(offset + 2)
        }
    }

    fun matchString(s: String, currentOffset: Int): Pair<Optional<TokenKeeper>, Int> {
        val tokensRange = currentOffset until s.length
        print("Try to find ${this.token} in ${s.substring(tokensRange)}... ")
        if(s[currentOffset] == ' ') return matchString(s, currentOffset+1) //Token can't start with space
        if(s.length <= currentOffset) return Pair(Optional.empty(), currentOffset)
        var substring: String = ""
        return when(token.repr.r) {
            Representation.None -> {
                println("going further with 'None' as $token.")
                this.matchFollowers(s, currentOffset)
            }
            Representation.CommonCharacter -> {
                val currentCharacter = s[currentOffset]
                return if(!"""\'""".contains(currentCharacter)){
                    println("$currentCharacter detected as ${this.token}")
                    this.set(currentCharacter.toString())
                    this.matchFollowers(s, currentOffset+1)
                } else {
                    Pair(Optional.empty(), currentOffset)
                }
            }
            Representation.Word -> {
                var nextOffset = currentOffset+1
                substring = s.substring(currentOffset until nextOffset)
                val resInitial = substring.matches("\\w+?".toRegex())
                var res = resInitial
                while (nextOffset<s.length && res) {
                    nextOffset += 1
                    substring = s.substring(currentOffset until nextOffset)
                    res = substring.matches("\\w+?".toRegex())
                }

                nextOffset -= 1
                substring = substring.substring(0 until substring.length-1)

                return if(resInitial) {
                    println("$substring detected as ${this.token}")
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
                    println("$passedString == $thisName, ${this.token} detected")
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
        if(s[currentOffset] == ' ') return matchFollowers(s, currentOffset+1) //Token can't start with space
        for((followers, importance) in thisFollowers) {

            val tokensRange = veryCurrentOffset until s.length
            println("    looking in '${s.substring(tokensRange)}' for any of {${followers.map { f -> f.toString() }} as follower for ${this.token}. Offset = $veryCurrentOffset")
            val fres = when(importance) {
                Importance.IsRequired -> false
                Importance.IsOptional -> true
            }

            val followersMatchResult = if (s.isNotEmpty()) {
                var r: Pair<Optional<TokenKeeper>, Int> = Pair(Optional.empty(), veryCurrentOffset)
                for(f in followers) {
                    val vco = veryCurrentOffset
                    val ri = TokenKeeper(f).matchString(s, veryCurrentOffset)
                    if (ri.first.isPresent) {
                        val child = ri.first.get()
                        //if(child.token == SqlToken.ANY_WORD) {
                        //    child.set(s[vco])
                       //}
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
        println("${this.token} ${mainres.isPresent} matched by followers. veryCurrentOffset = $veryCurrentOffset")
        return Pair(mainres, veryCurrentOffset)
    }
}

fun main(args: Array<String>) {
    //val queryString = "SELECT name, date FROM tutorials_tbl WHERE name = 'Vasia' and date > 0";
    val queryString = """SELECT name, date FROM ( SELECT * FROM ( SELECT * FROM tutorials_tbl WHERE name = 'Vasia\'' ) WHERE date > 0 ) WHERE name = 'Vasia\\' and date > 0"""
    val splittedQuery = queryString.split("( |\n|((?=,))|((?<=,))|((?='))|((?<=')))".toRegex())
        .filter { ch -> ch.isNotEmpty() }
        .toList()
    println(splittedQuery.toString())
    val res = TokenKeeper(SqlToken.SELECT).matchString(queryString, 0)
    if (res.second < splittedQuery.size - 1){
        throw Exception("Can't match string $queryString. Matched ${res.second} of ${splittedQuery.size} possible tokens")
    }
    println("Result = $res")
    println("Query tree:")
    res.first.map { r -> r.printAsATree(0) }
}