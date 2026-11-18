package com.example.data

enum class QubeeCategory {
    VOWEL,
    CONSONANT,
    DIGRAPH
}

data class QubeeLetter(
    val symbol: String,
    val name: String,
    val category: QubeeCategory,
    val soundGuideOm: String,
    val soundGuideEn: String,
    val exampleWord: String,
    val exampleMeaningEn: String
) {
    val ttsPronunciation: String
        get() = "$name. $exampleWord"
}

enum class PhraseCategory {
    GREETINGS,
    ESSENTIALS,
    TRAVEL,
    MARKET,
    FOOD,
    FAMILY
}

data class PhraseItem(
    val id: String,
    val category: PhraseCategory,
    val oromo: String,
    val english: String,
    val pronunciationTip: String
)

enum class ProverbCategory {
    WISDOM,
    UNITY,
    PATIENCE,
    TRUTH,
    WORK,
    NATURE,
    COMMUNITY,
    CHARACTER
}

data class ProverbItem(
    val id: String,
    val oromo: String,
    val english: String,
    val meaningOm: String,
    val meaningEn: String,
    val category: ProverbCategory = ProverbCategory.WISDOM
)

object OromoContent {

    @Volatile
    private var cachedProverbs: List<ProverbItem>? = null

    fun loadProverbs(context: android.content.Context): List<ProverbItem> {
        cachedProverbs?.let { return it }
        return synchronized(this) {
            cachedProverbs?.let { return it }
            val loaded = runCatching {
                val jsonText = context.assets.open("mammaaksa.json").bufferedReader().use { it.readText() }
                val array = org.json.JSONArray(jsonText)
                buildList(array.length()) {
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val category = runCatching {
                            ProverbCategory.valueOf(obj.optString("category", "WISDOM"))
                        }.getOrDefault(ProverbCategory.WISDOM)
                        add(
                            ProverbItem(
                                id = obj.getString("id"),
                                oromo = obj.getString("oromo"),
                                english = obj.getString("english"),
                                meaningOm = obj.getString("meaningOm"),
                                meaningEn = obj.getString("meaningEn"),
                                category = category
                            )
                        )
                    }
                }
            }.getOrNull()?.takeIf { it.isNotEmpty() } ?: proverbs
            cachedProverbs = loaded
            loaded
        }
    }

    @Volatile
    private var cachedPhrases: List<PhraseItem>? = null

    fun loadPhrases(context: android.content.Context): List<PhraseItem> {
        cachedPhrases?.let { return it }
        return synchronized(this) {
            cachedPhrases?.let { return it }
            val loaded = runCatching {
                val jsonText = context.assets.open("haasawa.json").bufferedReader().use { it.readText() }
                val array = org.json.JSONArray(jsonText)
                buildList(array.length()) {
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val category = runCatching {
                            PhraseCategory.valueOf(obj.optString("category", "GREETINGS"))
                        }.getOrDefault(PhraseCategory.GREETINGS)
                        add(
                            PhraseItem(
                                id = obj.getString("id"),
                                category = category,
                                oromo = obj.getString("oromo"),
                                english = obj.getString("english"),
                                pronunciationTip = obj.optString("pronunciationTip", "")
                            )
                        )
                    }
                }
            }.getOrNull()?.takeIf { it.isNotEmpty() } ?: phrases
            cachedPhrases = loaded
            loaded
        }
    }

    val proverbs: List<ProverbItem> = listOf(
        ProverbItem(
            id = "p1",
            oromo = "Harki tokko qofti harka hin dhiqatu.",
            english = "One hand alone cannot wash itself.",
            meaningOm = "Tokkummaan fi waliin hojjechuun humna guddaa uuma.",
            meaningEn = "Cooperation and community support are essential to achieve anything meaningful.",
            category = ProverbCategory.UNITY
        ),
        ProverbItem(
            id = "p2",
            oromo = "Suuta deeman suuta galu.",
            english = "Those who walk steadily arrive safely home.",
            meaningOm = "Obsaa fi tasgabbiin hojii cimaa milkeessa.",
            meaningEn = "Patience and steady persistence lead to lasting success.",
            category = ProverbCategory.PATIENCE
        ),
        ProverbItem(
            id = "p3",
            oromo = "Bishaan yaa'u hin xuraa'u.",
            english = "Flowing water never grows stagnant.",
            meaningOm = "Sochiin, barumsi fi hojiin itti fufiinsa qabu nama haaromsa.",
            meaningEn = "Continuous learning, action, and movement keep life vibrant and pure.",
            category = ProverbCategory.WORK
        ),
        ProverbItem(
            id = "p4",
            oromo = "Dubbiin beekaa gabaabduudha.",
            english = "The speech of a wise person is concise.",
            meaningOm = "Ogummaan baay'ina dubbii osoo hin taane qulqullina yaadaatiin mul'ata.",
            meaningEn = "True wisdom is measured by clarity and insight rather than many words.",
            category = ProverbCategory.WISDOM
        ),
        ProverbItem(
            id = "p5",
            oromo = "Qotee bulaan rooba hin sodaatu.",
            english = "The farmer does not fear the rain.",
            meaningOm = "Namni kaayyoo qabu rakkina bu'aa fidu hin baqatu.",
            meaningEn = "Someone committed to growth welcomes challenges that bring harvest.",
            category = ProverbCategory.WORK
        ),
        ProverbItem(
            id = "p6",
            oromo = "Gaarri gaaraan wal hin argu, namni namaan wal arga.",
            english = "Mountains never meet mountains, but people meet people.",
            meaningOm = "Hariiroo gaarii fi kabaja namaaf qabaachuun yeroo hunda barbaachisaadha.",
            meaningEn = "Treat others with kindness and respect because paths inevitably cross again.",
            category = ProverbCategory.COMMUNITY
        ),
        ProverbItem(
            id = "p7",
            oromo = "Kan har'a facaasan boru haammatu.",
            english = "What is sown today is harvested tomorrow.",
            meaningOm = "Carraaqqii fi gochi gaariin har'aa bu'aa boruu murteessa.",
            meaningEn = "Your daily habits and effort today shape the future you will reap.",
            category = ProverbCategory.WORK
        ),
        ProverbItem(
            id = "p8",
            oromo = "Gorsa didan duuba seenaa'a.",
            english = "Ignoring wise counsel brings regret later.",
            meaningOm = "Muuxannoo fi gorsa maanguddootaa dhaggeeffachuun kufaatii irraa nama oolcha.",
            meaningEn = "Listening to experienced guidance prevents avoidable mistakes.",
            category = ProverbCategory.WISDOM
        ),
        ProverbItem(
            id = "p9",
            oromo = "Mixiin wal gargaartee arba hiiti.",
            english = "United ants can tie up an elephant.",
            meaningOm = "Namoonni xixiqqoon yoo walii galan wanta guddaa hojjechuu danda'u.",
            meaningEn = "Collective unity overcomes even the most overwhelming obstacles.",
            category = ProverbCategory.UNITY
        ),
        ProverbItem(
            id = "p10",
            oromo = "Barumsi ifa jireenyaati.",
            english = "Education is the light of life.",
            meaningOm = "Beekumsi dukkana wallaalummaa balleessuun karaa haaraa bana.",
            meaningEn = "Knowledge illuminates the path forward and unlocks new possibilities.",
            category = ProverbCategory.WISDOM
        )
    )

    val qubeeLetters: List<QubeeLetter> = listOf(
        // 5 Vowels (Dubbachiiftuu)
        QubeeLetter("A a", "A", QubeeCategory.VOWEL, "Gabaabaa: 'a' akkuma 'mana'", "Short 'a' as in 'father'; long 'aa' held twice as long", "Abbaa", "Father"),
        QubeeLetter("E e", "E", QubeeCategory.VOWEL, "Gabaabaa: 'e' akkuma 'beela'", "Short 'e' as in 'bet'; long 'ee' like 'they'", "Eebba", "Blessing"),
        QubeeLetter("I i", "I", QubeeCategory.VOWEL, "Gabaabaa: 'i' akkuma 'bitaa'", "Short 'i' as in 'bit'; long 'ii' like 'machine'", "Ifa", "Light"),
        QubeeLetter("O o", "O", QubeeCategory.VOWEL, "Gabaabaa: 'o' akkuma 'bona'", "Short 'o' as in 'port'; long 'oo' held longer", "Oromoo", "Oromo"),
        QubeeLetter("U u", "U", QubeeCategory.VOWEL, "Gabaabaa: 'u' akkuma 'buna'", "Short 'u' as in 'put'; long 'uu' like 'moon'", "Urjii", "Star"),

        // Consonants (Dubbifamaa)
        QubeeLetter("B b", "Ba", QubeeCategory.CONSONANT, "Akkuma sagalee 'ba'", "Voiced 'b' as in 'book'", "Buna", "Coffee"),
        QubeeLetter("C c", "Ca", QubeeCategory.CONSONANT, "Sagalee jabaa ejective 'ch'", "Ejective 'ch' sound with a sharp burst of air", "Caffee", "Meadow / Assembly"),
        QubeeLetter("D d", "Da", QubeeCategory.CONSONANT, "Akkuma sagalee 'da'", "Voiced 'd' as in 'day'", "Dubbii", "Speech / Matter"),
        QubeeLetter("F f", "Fa", QubeeCategory.CONSONANT, "Akkuma sagalee 'fa'", "Voiceless 'f' as in 'fast'", "Farda", "Horse"),
        QubeeLetter("G g", "Ga", QubeeCategory.CONSONANT, "Akkuma sagalee 'ga'", "Hard 'g' as in 'go'", "Gaara", "Mountain"),
        QubeeLetter("H h", "Ha", QubeeCategory.CONSONANT, "Akkuma sagalee 'ha'", "Soft 'h' as in 'hello'", "Harka", "Hand"),
        QubeeLetter("J j", "Ja", QubeeCategory.CONSONANT, "Akkuma sagalee 'ja'", "Voiced 'j' as in 'joy'", "Jireenya", "Life"),
        QubeeLetter("K k", "Ka", QubeeCategory.CONSONANT, "Akkuma sagalee 'ka'", "Crisp 'k' as in 'kite'", "Kabaja", "Respect"),
        QubeeLetter("L l", "La", QubeeCategory.CONSONANT, "Akkuma sagalee 'la'", "Clear 'l' as in 'lamp'", "Lafa", "Earth / Land"),
        QubeeLetter("M m", "Ma", QubeeCategory.CONSONANT, "Akkuma sagalee 'ma'", "Bilabial 'm' as in 'moon'", "Mana", "House"),
        QubeeLetter("N n", "Na", QubeeCategory.CONSONANT, "Akkuma sagalee 'na'", "Alveolar 'n' as in 'name'", "Nagaa", "Peace"),
        QubeeLetter("P p", "Pa", QubeeCategory.CONSONANT, "Sagalee 'pa' (jechoota ergifannaa keessatti)", "Voiceless 'p' (mainly in loanwords)", "Poolisii", "Police"),
        QubeeLetter("Q q", "Qa", QubeeCategory.CONSONANT, "Sagalee jabaa ejective 'k'", "Ejective 'k' produced further back in the throat", "Qubee", "Letter / Alphabet"),
        QubeeLetter("R r", "Ra", QubeeCategory.CONSONANT, "Sagalee 'ra' (arrabaan rukutamu)", "Rolled or tapped 'r'", "Rooba", "Rain"),
        QubeeLetter("S s", "Sa", QubeeCategory.CONSONANT, "Akkuma sagalee 'sa'", "Voiceless 's' as in 'sun'", "Samii", "Sky"),
        QubeeLetter("T t", "Ta", QubeeCategory.CONSONANT, "Akkuma sagalee 'ta'", "Crisp 't' as in 'top'", "Tokkummaa", "Unity"),
        QubeeLetter("V v", "Va", QubeeCategory.CONSONANT, "Sagalee 'va' (jechoota ergifannaa)", "Voiced 'v' (primarily in loanwords)", "Viidiyoo", "Video"),
        QubeeLetter("W w", "Wa", QubeeCategory.CONSONANT, "Akkuma sagalee 'wa'", "Glide 'w' as in 'water'", "Waaqayyo", "God"),
        QubeeLetter("X x", "Xa", QubeeCategory.CONSONANT, "Sagalee jabaa ejective 't'", "Ejective 't' with a sharp popped release", "Xalayaa", "Letter (mail)"),
        QubeeLetter("Y y", "Ya", QubeeCategory.CONSONANT, "Akkuma sagalee 'ya'", "Palatal glide 'y' as in 'yes'", "Yaada", "Thought / Idea"),
        QubeeLetter("Z z", "Za", QubeeCategory.CONSONANT, "Akkuma sagalee 'za'", "Voiced 'z' as in 'zero'", "Zayita", "Oil"),

        // Digraphs (Qubee Dachaa)
        QubeeLetter("Ch ch", "Cha", QubeeCategory.DIGRAPH, "Sagalee laafaa 'ch' akkuma 'church'", "Soft 'ch' as in 'church'", "Chaa'uu", "To dry out"),
        QubeeLetter("Dh dh", "Dha", QubeeCategory.DIGRAPH, "Sagalee implosive 'd' gara keessaatti", "Implosive 'dh' made with inward airflow", "Dhugaa", "Truth"),
        QubeeLetter("Ny ny", "Nya", QubeeCategory.DIGRAPH, "Sagalee funyaanii 'ny' akkuma 'canyon'", "Palatal nasal 'ny' like 'canyon' or Spanish 'ñ'", "Nyaata", "Food"),
        QubeeLetter("Ph ph", "Pha", QubeeCategory.DIGRAPH, "Sagalee jabaa ejective 'p'", "Ejective 'p' with a crisp burst", "Aphoo", "Mouthful / Bit"),
        QubeeLetter("Sh sh", "Sha", QubeeCategory.DIGRAPH, "Sagalee 'sh' akkuma 'shine'", "Voiceless 'sh' as in 'shine'", "Shanan", "Five"),
        QubeeLetter("Ts ts", "Tsa", QubeeCategory.DIGRAPH, "Sagalee 'ts' walitti qabame", "Affricate 'ts' as in 'cats'", "Tsaaduu", "Loanword affricate"),
        QubeeLetter("Zy zy", "Zya", QubeeCategory.DIGRAPH, "Sagalee 'zh' akkuma 'measure'", "Voiced postalveolar 'zh' as in 'measure'", "Zyaana", "Term variant")
    )

    val phrases: List<PhraseItem> = listOf(
        // Greetings (12)
        PhraseItem("g1", PhraseCategory.GREETINGS, "Akkam jirta?", "How are you? (singular)", "ah-kahm jeer-tah"),
        PhraseItem("g2", PhraseCategory.GREETINGS, "Akkam jirtu?", "How are you? (plural / respectful)", "ah-kahm jeer-too"),
        PhraseItem("g3", PhraseCategory.GREETINGS, "Nagaa koo dha, galatoomi.", "I am fine, thank you.", "nah-gaa koh dah, gah-lah-toh-mee"),
        PhraseItem("g4", PhraseCategory.GREETINGS, "Akkam bultan?", "Good morning! (How did you spend the night?)", "ah-kahm bool-tahn"),
        PhraseItem("g5", PhraseCategory.GREETINGS, "Akkam ooltan?", "Good afternoon! (How did you spend the day?)", "ah-kahm ohl-tahn"),
        PhraseItem("g6", PhraseCategory.GREETINGS, "Nagaan bulaa.", "Good night / Sleep peacefully.", "nah-gaan boo-laa"),
        PhraseItem("g7", PhraseCategory.GREETINGS, "Baga nagaan dhuftan!", "Welcome! (plural / respectful)", "bah-gah nah-gaan dhoof-tahn"),
        PhraseItem("g8", PhraseCategory.GREETINGS, "Baga nagaan na eegde.", "Thank you for welcoming me warmly.", "bah-gah nah-gaan nah eeg-deh"),
        PhraseItem("g9", PhraseCategory.GREETINGS, "Maatiin kee akkam jiru?", "How is your family doing?", "maa-teen kee ah-kahm jee-roo"),
        PhraseItem("g10", PhraseCategory.GREETINGS, "Hundi isaanii nagaa qabu.", "Everyone is doing well.", "hoon-dee ee-saa-nee nah-gaa qah-boo"),
        PhraseItem("g11", PhraseCategory.GREETINGS, "Boru wal argina.", "See you tomorrow.", "boh-roo wahl ahr-gee-nah"),
        PhraseItem("g12", PhraseCategory.GREETINGS, "Nagaan oolaa / Nagaan galaa.", "Have a peaceful day / Safe journey home.", "nah-gaan oh-laa / nah-gaan gah-laa"),

        // Essentials (10)
        PhraseItem("e1", PhraseCategory.ESSENTIALS, "Galatoomi / Galatoomaa", "Thank you (singular / plural)", "gah-lah-toh-mee"),
        PhraseItem("e2", PhraseCategory.ESSENTIALS, "Maaloo, na gargaari.", "Please, help me.", "maa-loh, nah gahr-gaa-ree"),
        PhraseItem("e3", PhraseCategory.ESSENTIALS, "Dhiifama naaf godhi.", "Excuse me / Please forgive me.", "dhee-fah-mah naaf goh-dee"),
        PhraseItem("e4", PhraseCategory.ESSENTIALS, "Eeyyee / Lakki", "Yes / No", "ey-yee / lahk-kee"),
        PhraseItem("e5", PhraseCategory.ESSENTIALS, "Maqaan koo ... jedhama.", "My name is ...", "mah-qaan koh ... jeh-dah-mah"),
        PhraseItem("e6", PhraseCategory.ESSENTIALS, "Maqaan kee eenyu?", "What is your name?", "mah-qaan kee een-yoo"),
        PhraseItem("e7", PhraseCategory.ESSENTIALS, "Hin hubanne, irra deebi'i maaloo.", "I did not understand; please repeat.", "hin hoo-bahn-neh, eer-rah dee-bee-ee"),
        PhraseItem("e8", PhraseCategory.ESSENTIALS, "Suuta dubbadhu, maaloo.", "Please speak slowly.", "soo-tah doob-bah-dhoo, maa-loh"),
        PhraseItem("e9", PhraseCategory.ESSENTIALS, "Afaan Oromoo barachaan jira.", "I am learning Afaan Oromo.", "ah-faan oh-roh-moh bah-rah-chaan jee-rah"),
        PhraseItem("e10", PhraseCategory.ESSENTIALS, "Rakkoon hin jiru.", "No problem / Everything is okay.", "rahk-kohn hin jee-roo"),

        // Travel (10)
        PhraseItem("t1", PhraseCategory.TRAVEL, "Karaan gara ... eessa?", "Where is the road to ...?", "kah-raan gah-rah ... ees-sah"),
        PhraseItem("t2", PhraseCategory.TRAVEL, "Magaalaan asirraa fagaataa?", "Is the town far from here?", "mah-gaa-laan ah-seer-raa fah-gaa-taa"),
        PhraseItem("t3", PhraseCategory.TRAVEL, "Buufanni konkolaataa eessa?", "Where is the bus station?", "boo-fahn-nee kohn-koh-laa-taa ees-sah"),
        PhraseItem("t4", PhraseCategory.TRAVEL, "Asitti na buusi, maaloo.", "Please drop me off here.", "ah-sit-tee nah boo-see, maa-loh"),
        PhraseItem("t5", PhraseCategory.TRAVEL, "Konkolaataan kun gara ... deemaa?", "Does this vehicle go toward ...?", "kohn-koh-laa-taan koon gah-rah ... dee-maa"),
        PhraseItem("t6", PhraseCategory.TRAVEL, "Tikkeettii tokko naaf kenni.", "Please give me one ticket.", "teek-keet-tee tohk-koh naaf kehn-nee"),
        PhraseItem("t7", PhraseCategory.TRAVEL, "Hoteelli dhihoon eessatti argama?", "Where is a nearby hotel located?", "hoh-teel-lee dhee-hohn ees-saht-tee ahr-gah-mah"),
        PhraseItem("t8", PhraseCategory.TRAVEL, "Sa'aatiin meeqa ta'e?", "What time is it?", "sah-aa-teen mee-qah tah-eh"),
        PhraseItem("t9", PhraseCategory.TRAVEL, "Mirga moo bitaatti gora?", "Should I turn right or left?", "meer-gah moh bee-taat-tee goh-rah"),
        PhraseItem("t10", PhraseCategory.TRAVEL, "Imalli keenya baay'ee gaarii ture.", "Our trip was very pleasant.", "ee-mahl-lee keen-yah baay-ee gaa-ree too-reh"),

        // Market (10)
        PhraseItem("m1", PhraseCategory.MARKET, "Gatiin isaa meeqa?", "How much does this cost?", "gah-teen ee-saa mee-qah"),
        PhraseItem("m2", PhraseCategory.MARKET, "Hir'isuu dandeessaa?", "Can you lower the price?", "hir-ee-soo dahn-dees-saa"),
        PhraseItem("m3", PhraseCategory.MARKET, "Kana bitachuu barbaada.", "I want to buy this.", "kah-nah bee-tah-choo bahr-baa-dah"),
        PhraseItem("m4", PhraseCategory.MARKET, "Kun baay'ee mi'aadha.", "This is too expensive.", "koon baay-ee mee-aa-dah"),
        PhraseItem("m5", PhraseCategory.MARKET, "Kiiloo tokko naaf madaali.", "Please weigh one kilogram for me.", "kee-loh tohk-koh naaf mah-daa-lee"),
        PhraseItem("m6", PhraseCategory.MARKET, "Kan biraa qabduu?", "Do you have another one?", "kahn bee-raa qahb-doo"),
        PhraseItem("m7", PhraseCategory.MARKET, "Qarshii jijjiirraa qabdaa?", "Do you have change?", "qahr-shee jeej-jeer-raa qahb-daa"),
        PhraseItem("m8", PhraseCategory.MARKET, "Haaraa fi qulqulluudhaa?", "Is it fresh and clean?", "haa-raa fee qool-qool-loo-daa"),
        PhraseItem("m9", PhraseCategory.MARKET, "Shaqaxni kun baay'ee bareedaadha.", "These goods are very nice.", "shah-qahx-nee koon baay-ee bah-ree-daa-dah"),
        PhraseItem("m10", PhraseCategory.MARKET, "Galatoomi, nagaan ooli!", "Thank you, have a good day!", "gah-lah-toh-mee, nah-gaan oh-lee"),

        // Food & Coffee (10)
        PhraseItem("f1", PhraseCategory.FOOD, "Bishaan dhugaatii naaf fidi, maaloo.", "Please bring me drinking water.", "bee-shaan dhoo-gaa-tee naaf fee-dee"),
        PhraseItem("f2", PhraseCategory.FOOD, "Bunni baay'ee mi'aawa.", "The coffee is very delicious.", "boon-nee baay-ee mee-ah-wah"),
        PhraseItem("f3", PhraseCategory.FOOD, "Nyaanni gaariidha!", "The food is great!", "nyaan-nee gaa-ree-dah"),
        PhraseItem("f4", PhraseCategory.FOOD, "Beela'eera / Dheebodheera.", "I am hungry / I am thirsty.", "bee-lah-ee-rah / dhee-boh-dhee-rah"),
        PhraseItem("f5", PhraseCategory.FOOD, "Buna siin dhuguu danda'aa?", "May I drink coffee with you?", "boo-nah seen dhoo-goo dahn-dah-aa"),
        PhraseItem("f6", PhraseCategory.FOOD, "Shayii ho'aa tokko naaf kenni.", "Please give me one hot tea.", "shah-yee hoh-aa tohk-koh naaf kehn-nee"),
        PhraseItem("f7", PhraseCategory.FOOD, "Caccabsaan kun baay'ee mi'aawa.", "This chechebsa is very tasty.", "chah-chahb-saan koon baay-ee mee-ah-wah"),
        PhraseItem("f8", PhraseCategory.FOOD, "Sukkaara xiqqoo itti dabali.", "Add a little sugar to it.", "sook-kaa-rah xeeq-qoh eet-tee dah-bah-lee"),
        PhraseItem("f9", PhraseCategory.FOOD, "Quufeera, galatoomi.", "I am full, thank you.", "qoo-fee-rah, gah-lah-toh-mee"),
        PhraseItem("f10", PhraseCategory.FOOD, "Kaffaltiin meeqa ta'e?", "What is the total bill?", "kahf-fahl-teen mee-qah tah-eh"),

        // Family & Home (8)
        PhraseItem("fm1", PhraseCategory.FAMILY, "Manni kee eessa?", "Where is your home?", "mahn-nee kee ees-sah"),
        PhraseItem("fm2", PhraseCategory.FAMILY, "Abbaa fi haati koo nagaadha.", "My father and mother are well.", "ahb-baa fee haa-tee koh nah-gaa-dah"),
        PhraseItem("fm3", PhraseCategory.FAMILY, "Obboleessa fi obboleettii qabdaa?", "Do you have a brother and sister?", "ohb-boh-lees-sah fee ohb-boh-leet-tee qahb-daa"),
        PhraseItem("fm4", PhraseCategory.FAMILY, "Ol seeni, taa'i maaloo.", "Come inside and have a seat, please.", "ohl see-nee, taa-ee maa-loh"),
        PhraseItem("fm5", PhraseCategory.FAMILY, "Mana keenya keessatti baga nagaan dhufte.", "Welcome into our home.", "mah-nah keen-yah kees-saht-tee bah-gah nah-gaan dhoof-teh"),
        PhraseItem("fm6", PhraseCategory.FAMILY, "Ijoolleen barumsa jirti.", "The children are at school.", "ee-johl-leen bah-room-sah jeer-tee"),
        PhraseItem("fm7", PhraseCategory.FAMILY, "Waliin haa haasofnu.", "Let us chat together.", "wah-leen haa haa-sohf-noo"),
        PhraseItem("fm8", PhraseCategory.FAMILY, "Jaalala fi kabaja isiniif qaba.", "I have love and respect for you all.", "jaa-lah-lah fee kah-bah-jah ee-see-neef qah-bah")
    )
}
