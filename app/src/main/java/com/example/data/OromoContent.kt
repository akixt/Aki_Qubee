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
)

enum class PhraseCategory {
    GREETINGS,
    ESSENTIALS,
    TRAVEL,
    MARKET,
    FOOD
}

data class PhraseItem(
    val id: String,
    val category: PhraseCategory,
    val oromo: String,
    val english: String,
    val pronunciationTip: String
)

data class ProverbItem(
    val id: String,
    val oromo: String,
    val english: String,
    val meaningOm: String,
    val meaningEn: String
)

object OromoContent {

    val proverbs: List<ProverbItem> = listOf(
        ProverbItem(
            id = "p1",
            oromo = "Harki tokko qofti harka hin dhiqatu.",
            english = "One hand alone cannot wash itself.",
            meaningOm = "Tokkummaan fi waliin hojjechuun humna guddaa uuma.",
            meaningEn = "Cooperation and community support are essential to achieve anything meaningful."
        ),
        ProverbItem(
            id = "p2",
            oromo = "Suuta deeman suuta galu.",
            english = "Those who walk steadily arrive safely home.",
            meaningOm = "Obsaa fi tasgabbiin hojii cimaa milkeessa.",
            meaningEn = "Patience and steady persistence lead to lasting success."
        ),
        ProverbItem(
            id = "p3",
            oromo = "Bishaan yaa'u hin xuraa'u.",
            english = "Flowing water never grows stagnant.",
            meaningOm = "Sochiin, barumsi fi hojiin itti fufiinsa qabu nama haaromsa.",
            meaningEn = "Continuous learning, action, and movement keep life vibrant and pure."
        ),
        ProverbItem(
            id = "p4",
            oromo = "Dubbiin beekaa gabaabduudha.",
            english = "The speech of a wise person is concise.",
            meaningOm = "Ogummaan baay'ina dubbii osoo hin taane qulqullina yaadaatiin mul'ata.",
            meaningEn = "True wisdom is measured by clarity and insight rather than many words."
        ),
        ProverbItem(
            id = "p5",
            oromo = "Qotee bulaan rooba hin sodaatu.",
            english = "The farmer does not fear the rain.",
            meaningOm = "Namni kaayyoo qabu rakkina bu'aa fidu hin baqatu.",
            meaningEn = "Someone committed to growth welcomes challenges that bring harvest."
        ),
        ProverbItem(
            id = "p6",
            oromo = "Gaarri gaaraan wal hin argu, namni namaan wal arga.",
            english = "Mountains never meet mountains, but people meet people.",
            meaningOm = "Hariiroo gaarii fi kabaja namaaf qabaachuun yeroo hunda barbaachisaadha.",
            meaningEn = "Treat others with kindness and respect because paths inevitably cross again."
        ),
        ProverbItem(
            id = "p7",
            oromo = "Kan har'a facaasan boru haammatu.",
            english = "What is sown today is harvested tomorrow.",
            meaningOm = "Carraaqqii fi gochi gaariin har'aa bu'aa boruu murteessa.",
            meaningEn = "Your daily habits and effort today shape the future you will reap."
        ),
        ProverbItem(
            id = "p8",
            oromo = "Gorsa didan duuba seenaa'a.",
            english = "Ignoring wise counsel brings regret later.",
            meaningOm = "Muuxannoo fi gorsa maanguddootaa dhaggeeffachuun kufaatii irraa nama oolcha.",
            meaningEn = "Listening to experienced guidance prevents avoidable mistakes."
        ),
        ProverbItem(
            id = "p9",
            oromo = "Mixiin wal gargaartee arba hiiti.",
            english = "United ants can tie up an elephant.",
            meaningOm = "Namoonni xixiqqoon yoo walii galan wanta guddaa hojjechuu danda'u.",
            meaningEn = "Collective unity overcomes even the most overwhelming obstacles."
        ),
        ProverbItem(
            id = "p10",
            oromo = "Barumsi ifa jireenyaati.",
            english = "Education is the light of life.",
            meaningOm = "Beekumsi dukkana wallaalummaa balleessuun karaa haaraa bana.",
            meaningEn = "Knowledge illuminates the path forward and unlocks new possibilities."
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
        // Greetings
        PhraseItem("g1", PhraseCategory.GREETINGS, "Akkam jirta?", "How are you? (singular)", "ah-kahm jeer-tah"),
        PhraseItem("g2", PhraseCategory.GREETINGS, "Akkam jirtu?", "How are you? (plural / respectful)", "ah-kahm jeer-too"),
        PhraseItem("g3", PhraseCategory.GREETINGS, "Nagaa koo dha, galatoomi.", "I am fine, thank you.", "nah-gaa koh dah, gah-lah-toh-mee"),
        PhraseItem("g4", PhraseCategory.GREETINGS, "Akkam bultan?", "Good morning! (lit. How did you spend the night?)", "ah-kahm bool-tahn"),
        PhraseItem("g5", PhraseCategory.GREETINGS, "Akkam ooltan?", "Good afternoon! (lit. How did you spend the day?)", "ah-kahm ohl-tahn"),
        PhraseItem("g6", PhraseCategory.GREETINGS, "Nagaan bulaa.", "Good night / Sleep peacefully.", "nah-gaan boo-laa"),

        // Essentials
        PhraseItem("e1", PhraseCategory.ESSENTIALS, "Galatoomi / Galatoomaa", "Thank you (singular / plural)", "gah-lah-toh-mee"),
        PhraseItem("e2", PhraseCategory.ESSENTIALS, "Maaloo", "Please", "maa-loh"),
        PhraseItem("e3", PhraseCategory.ESSENTIALS, "Dhiifama", "Excuse me / Sorry", "dhee-fah-mah"),
        PhraseItem("e4", PhraseCategory.ESSENTIALS, "Eeyyee / Lakki", "Yes / No", "ey-yee / lahk-kee"),
        PhraseItem("e5", PhraseCategory.ESSENTIALS, "Maqaan koo ... jedhama.", "My name is ...", "mah-qaan koh ... jeh-dah-mah"),
        PhraseItem("e6", PhraseCategory.ESSENTIALS, "Maqaan kee eenyu?", "What is your name?", "mah-qaan kee een-yoo"),
        PhraseItem("e7", PhraseCategory.ESSENTIALS, "Hin hubanne.", "I did not understand.", "hin hoo-bahn-neh"),

        // Travel
        PhraseItem("t1", PhraseCategory.TRAVEL, "Karaan gara ... eessa?", "Where is the road to ...?", "kah-raan gah-rah ... ees-sah"),
        PhraseItem("t2", PhraseCategory.TRAVEL, "Magaalaan fagaataa?", "Is the city far?", "mah-gaa-laan fah-gaa-taa"),
        PhraseItem("t3", PhraseCategory.TRAVEL, "Buufanni konkolaataa eessa?", "Where is the bus station?", "boo-fahn-nee kohn-koh-laa-taa ees-sah"),
        PhraseItem("t4", PhraseCategory.TRAVEL, "Asitti na buusi, maaloo.", "Drop me off here, please.", "ah-sit-tee nah boo-see, maa-loh"),

        // Market
        PhraseItem("m1", PhraseCategory.MARKET, "Gatiin isaa meeqa?", "How much does this cost?", "gah-teen ee-saa mee-qah"),
        PhraseItem("m2", PhraseCategory.MARKET, "Hir'isuu dandeessaa?", "Can you lower the price?", "hir-ee-soo dahn-dees-saa"),
        PhraseItem("m3", PhraseCategory.MARKET, "Kana bitachuu barbaada.", "I want to buy this.", "kah-nah bee-tah-choo bahr-baa-dah"),

        // Food
        PhraseItem("f1", PhraseCategory.FOOD, "Bishaan dhugaatii naaf fidi, maaloo.", "Please bring me drinking water.", "bee-shaan dhoo-gaa-tee naaf fee-dee"),
        PhraseItem("f2", PhraseCategory.FOOD, "Bunni baay'ee mi'aawa.", "The coffee is very delicious.", "boon-nee baay-ee mee-ah-wah"),
        PhraseItem("f3", PhraseCategory.FOOD, "Nyaanni gaariidha!", "The food is great!", "nyaan-nee gaa-ree-dah"),
        PhraseItem("f4", PhraseCategory.FOOD, "Beela'eera / Dheebodheera.", "I am hungry / I am thirsty.", "bee-lah-ee-rah / dhee-boh-dhee-rah")
    )
}
