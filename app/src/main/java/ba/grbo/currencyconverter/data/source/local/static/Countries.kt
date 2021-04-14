package ba.grbo.currencyconverter.data.source.local.static

import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.Code.*
import ba.grbo.currencyconverter.data.models.Country
import ba.grbo.currencyconverter.data.models.Currency

object Countries {
    val value = listOf(
            Country(
                    Currency(R.string.currency_united_arab_emirates, AED),
                    R.drawable.ic_flag_united_arab_emirates
            ),
            Country(Currency(R.string.currency_afghanistan, AFN), R.drawable.ic_flag_afghanistan),
            Country(Currency(R.string.currency_albania, ALL), R.drawable.ic_flag_albania),
            Country(Currency(R.string.currency_armenia, AMD), R.drawable.ic_flag_armenia),
            Country(
                    Currency(R.string.currency_netherlands, ANG),
                    R.drawable.ic_flag_netherlands
            ),
            Country(Currency(R.string.currency_angola, AOA), R.drawable.ic_flag_angola),
            Country(Currency(R.string.currency_argentina, ARS), R.drawable.ic_flag_argentina),
            Country(Currency(R.string.currency_australia, AUD), R.drawable.ic_flag_australia),
            Country(Currency(R.string.currency_aruba, AWG), R.drawable.ic_flag_aruba),
            Country(Currency(R.string.currency_azerbaijan, AZN), R.drawable.ic_flag_azerbaijan),
            Country(
                    Currency(R.string.currency_bosnia_and_herzegovina, BAM),
                    R.drawable.ic_flag_bosnia_and_herzegovina
            ),
            Country(Currency(R.string.currency_barbados, BBD), R.drawable.ic_flag_barbados),
            Country(Currency(R.string.currency_bangladesh, BDT), R.drawable.ic_flag_bangladesh),
            Country(Currency(R.string.currency_bulgaria, BGN), R.drawable.ic_flag_bulgaria),
            Country(Currency(R.string.currency_bahrain, BHD), R.drawable.ic_flag_bahrain),
            Country(Currency(R.string.currency_burundi, BIF), R.drawable.ic_flag_burundi),
            Country(Currency(R.string.currency_bermuda, BMD), R.drawable.ic_flag_bermuda),
            Country(Currency(R.string.currency_brunei, BND), R.drawable.ic_flag_brunei),
            Country(Currency(R.string.currency_bolivia, BOB), R.drawable.ic_flag_bolivia),
            Country(Currency(R.string.currency_brazil, BRL), R.drawable.ic_flag_brazil),
            Country(Currency(R.string.currency_bahamas, BSD), R.drawable.ic_flag_bahamas),
            Country(Currency(R.string.currency_bitcoin, BTC), R.drawable.ic_bitcoin),
            Country(Currency(R.string.currency_bhutan, BTN), R.drawable.ic_flag_bhutan),
            Country(Currency(R.string.currency_botswana, BWP), R.drawable.ic_flag_botswana),
            Country(Currency(R.string.currency_belarus, BYN), R.drawable.ic_flag_belarus),
            Country(Currency(R.string.currency_belize, BZN), R.drawable.ic_flag_belize),
            Country(Currency(R.string.currency_canada, CAD), R.drawable.ic_flag_canada),
            Country(
                    Currency(R.string.currency_democratic_republic_of_the_congo, CDF),
                    R.drawable.ic_flag_democratic_republic_of_the_congo
            ),
            Country(Currency(R.string.currency_switzerland, CHF), R.drawable.ic_flag_switzerland),
            Country(Currency(R.string.currency_chile_uf, CLF), R.drawable.ic_flag_chile),
            Country(Currency(R.string.currency_chile, CLP), R.drawable.ic_flag_chile),
            Country(Currency(R.string.currency_china_offshore, CNH), R.drawable.ic_flag_china),
            Country(Currency(R.string.currency_china, CNY), R.drawable.ic_flag_china),
            Country(Currency(R.string.currency_colombia, COP), R.drawable.ic_flag_colombia),
            Country(Currency(R.string.currency_costa_rica, CRC), R.drawable.ic_flag_costa_rica),
            Country(Currency(R.string.currency_cuba_convertible, CUC), R.drawable.ic_flag_cuba),
            Country(Currency(R.string.currency_cuba, CUP), R.drawable.ic_flag_cuba),
            Country(Currency(R.string.currency_cape_verde, CVE), R.drawable.ic_flag_cape_verde),
            Country(
                    Currency(R.string.currency_czech_republic, CZK),
                    R.drawable.ic_flag_czech_republic
            ),
            Country(Currency(R.string.currency_djibouti, DJF), R.drawable.ic_flag_djibouti),
            Country(Currency(R.string.currency_denmark, DKK), R.drawable.ic_flag_denmark),
            Country(
                    Currency(R.string.currency_dominican_republik, DOP),
                    R.drawable.ic_flag_dominican_republic
            ),
            Country(Currency(R.string.currency_algeria, DZD), R.drawable.ic_flag_algeria),
            Country(Currency(R.string.currency_egypt, EGP), R.drawable.ic_flag_egypt),
            Country(Currency(R.string.currency_eritrea, ERN), R.drawable.ic_flag_eritrea),
            Country(Currency(R.string.currency_ethiopia, ETB), R.drawable.ic_flag_ethiopia),
            Country(
                    Currency(R.string.currency_european_union, EUR),
                    R.drawable.ic_flag_european_union
            ),
            Country(Currency(R.string.currency_fiji, FJD), R.drawable.ic_flag_fiji),
            Country(
                    Currency(R.string.currency_falkland_islands, FKP),
                    R.drawable.ic_flag_falkland_islands
            ),
            Country(
                    Currency(R.string.currency_united_kingdom, GBP),
                    R.drawable.ic_flag_united_kingdom
            ),
            Country(Currency(R.string.currency_georgia, GEL), R.drawable.ic_flag_georgia),
            Country(Currency(R.string.currency_guersney, GGP), R.drawable.ic_flag_guernsey),
            Country(Currency(R.string.currency_ghana, GHS), R.drawable.ic_flag_ghana),
            Country(Currency(R.string.currency_gibraltar, GIP), R.drawable.ic_flag_gibraltar),
            Country(Currency(R.string.currency_gambia, GMD), R.drawable.ic_flag_gambia),
            Country(Currency(R.string.currency_guinea, GNF), R.drawable.ic_flag_guinea),
            Country(Currency(R.string.currency_guatemala, GTQ), R.drawable.ic_flag_guatemala),
            Country(Currency(R.string.currency_guyana, GYD), R.drawable.ic_flag_guyana),
            Country(Currency(R.string.currency_hong_kong, HKD), R.drawable.ic_flag_hong_kong),
            Country(Currency(R.string.currency_honduras, HNL), R.drawable.ic_flag_honduras),
            Country(Currency(R.string.currency_croatia, HRK), R.drawable.ic_flag_croatia),
            Country(Currency(R.string.currency_haiti, HTG), R.drawable.ic_flag_haiti),
            Country(Currency(R.string.currency_hungary, HUF), R.drawable.ic_flag_hungary),
            Country(Currency(R.string.currency_indonesia, IDR), R.drawable.ic_flag_indonesia),
            Country(Currency(R.string.currency_israel, ILS), R.drawable.ic_flag_israel),
            Country(
                    Currency(R.string.currency_isle_of_man, IMP),
                    R.drawable.ic_flag_isle_of_man
            ),
            Country(Currency(R.string.currency_india, INR), R.drawable.ic_flag_india),
            Country(Currency(R.string.currency_iraq, IQD), R.drawable.ic_flag_iraq),
            Country(Currency(R.string.currency_iran, IRR), R.drawable.ic_flag_iran),
            Country(Currency(R.string.currency_iceland, ISK), R.drawable.ic_flag_iceland),
            Country(Currency(R.string.currency_jersey, JEP), R.drawable.ic_flag_jersey),
            Country(Currency(R.string.currency_jamaica, JMD), R.drawable.ic_flag_jamaica),
            Country(Currency(R.string.currency_jordan, JOD), R.drawable.ic_flag_jordan),
            Country(Currency(R.string.currency_japan, JPY), R.drawable.ic_flag_japan),
            Country(Currency(R.string.currency_kenya, KES), R.drawable.ic_flag_kenya),
            Country(Currency(R.string.currency_kyrgyzstan, KGS), R.drawable.ic_flag_kyrgyzstan),
            Country(Currency(R.string.currency_cambodia, KHR), R.drawable.ic_flag_cambodia),
            Country(Currency(R.string.currency_comoros, KMF), R.drawable.ic_flag_comoros),
            Country(
                    Currency(R.string.currency_north_korea, KPW),
                    R.drawable.ic_flag_north_korea
            ),
            Country(
                    Currency(R.string.currency_south_korea, KRW),
                    R.drawable.ic_flag_south_korea
            ),
            Country(Currency(R.string.currency_kuwait, KWD), R.drawable.ic_flag_kuwait),
            Country(
                    Currency(R.string.currency_cayman_islands, KYD),
                    R.drawable.ic_flag_cayman_islands
            ),
            Country(Currency(R.string.currency_kazakhstan, KZT), R.drawable.ic_flag_kazakhstan),
            Country(Currency(R.string.currency_laos, LAK), R.drawable.ic_flag_laos),
            Country(Currency(R.string.currency_lebanon, LBP), R.drawable.ic_flag_lebanon),
            Country(Currency(R.string.currency_sri_lanka, LKR), R.drawable.ic_flag_sri_lanka),
            Country(Currency(R.string.currency_liberia, LRD), R.drawable.ic_flag_liberia),
            Country(Currency(R.string.currency_lesotho, LSL), R.drawable.ic_flag_lesotho),
            Country(Currency(R.string.currency_libya, LYD), R.drawable.ic_flag_libya),
            Country(Currency(R.string.currency_morocco, MAD), R.drawable.ic_flag_morocco),
            Country(Currency(R.string.currency_moldova, MDL), R.drawable.ic_flag_moldova),
            Country(Currency(R.string.currency_madagascar, MGA), R.drawable.ic_flag_madagascar),
            Country(
                    Currency(R.string.currency_north_macedonia, MKD),
                    R.drawable.ic_flag_north_macedonia
            ),
            Country(Currency(R.string.currency_myanmar, MMK), R.drawable.ic_flag_myanmar),
            Country(Currency(R.string.currency_mongolia, MNT), R.drawable.ic_flag_mongolia),
            Country(Currency(R.string.currency_macau, MOP), R.drawable.ic_flag_macau),
            Country(Currency(R.string.currency_mauritania, MRU), R.drawable.ic_flag_mauritania),
            Country(Currency(R.string.currency_mauritius, MUR), R.drawable.ic_flag_mauritius),
            Country(Currency(R.string.currency_maldives, MVR), R.drawable.ic_flag_maldives),
            Country(Currency(R.string.currency_malawi, MWK), R.drawable.ic_flag_malawi),
            Country(Currency(R.string.currency_mexico, MXN), R.drawable.ic_flag_mexico),
            Country(Currency(R.string.currency_malaysia, MYR), R.drawable.ic_flag_malaysia),
            Country(Currency(R.string.currency_mozambique, MZN), R.drawable.ic_flag_mozambique),
            Country(Currency(R.string.currency_namibia, NAD), R.drawable.ic_flag_namibia),
            Country(Currency(R.string.currency_nigeria, NGN), R.drawable.ic_flag_nigeria),
            Country(Currency(R.string.currency_nicaragua, NIO), R.drawable.ic_flag_nicaragua),
            Country(Currency(R.string.currency_norway, NOK), R.drawable.ic_flag_norway),
            Country(Currency(R.string.currency_nepal, NPR), R.drawable.ic_flag_nepal),
            Country(Currency(R.string.currency_new_zealand, NZD), R.drawable.ic_flag_new_zealand),
            Country(Currency(R.string.currency_oman, OMR), R.drawable.ic_flag_oman),
            Country(Currency(R.string.currency_panama, PAB), R.drawable.ic_flag_panama),
            Country(Currency(R.string.currency_peru, PEN), R.drawable.ic_flag_peru),
            Country(
                    Currency(R.string.currency_papua_new_guinea, PGK),
                    R.drawable.ic_flag_papua_new_guinea
            ),
            Country(
                    Currency(R.string.currency_philippines, PHP),
                    R.drawable.ic_flag_philippines
            ),
            Country(Currency(R.string.currency_pakistan, PKR), R.drawable.ic_flag_pakistan),
            Country(Currency(R.string.currency_poland, PLN), R.drawable.ic_flag_poland),
            Country(Currency(R.string.currency_paraguay, PYG), R.drawable.ic_flag_paraguay),
            Country(Currency(R.string.currency_qatar, QAR), R.drawable.ic_flag_qatar),
            Country(Currency(R.string.currency_romania, RON), R.drawable.ic_flag_romania),
            Country(Currency(R.string.currency_serbia, RSD), R.drawable.ic_flag_serbia),
            Country(Currency(R.string.currency_russia, RUB), R.drawable.ic_flag_russia),
            Country(Currency(R.string.currency_rwanda, RWF), R.drawable.ic_flag_rwanda),
            Country(
                    Currency(R.string.currency_saudi_arabia, SAR),
                    R.drawable.ic_flag_saudi_arabia
            ),
            Country(
                    Currency(R.string.currency_solomon_islands, SBD),
                    R.drawable.ic_flag_solomon_islands
            ),
            Country(Currency(R.string.currency_seychelles, SCR), R.drawable.ic_flag_seychelles),
            Country(Currency(R.string.currency_sudan, SDG), R.drawable.ic_flag_sudan),
            Country(Currency(R.string.currency_sweden, SEK), R.drawable.ic_flag_sweden),
            Country(Currency(R.string.currency_singapore, SGD), R.drawable.ic_flag_singapore),
            Country(
                    Currency(R.string.currency_sierra_leone, SLL),
                    R.drawable.ic_flag_sierra_leone
            ),
            Country(Currency(R.string.currency_somalia, SOS), R.drawable.ic_flag_somalia),
            Country(Currency(R.string.currency_suriname, SRD), R.drawable.ic_flag_suriname),
            Country(
                    Currency(R.string.currency_south_sudan, SSP),
                    R.drawable.ic_flag_south_sudan
            ),
            Country(
                    Currency(R.string.currency_sao_tome_and_principe, STN),
                    R.drawable.ic_flag_sao_tome_and_principe
            ),
            Country(
                    Currency(R.string.currency_el_salvador, SVC),
                    R.drawable.ic_flag_el_salvador
            ),
            Country(Currency(R.string.currency_syria, SYP), R.drawable.ic_flag_syria),
            Country(Currency(R.string.currency_swaziland, SZL), R.drawable.ic_flag_swaziland),
            Country(Currency(R.string.currency_thailand, THB), R.drawable.ic_flag_thailand),
            Country(Currency(R.string.currency_tajikistan, TJS), R.drawable.ic_flag_tajikistan),
            Country(
                    Currency(R.string.currency_turkmenistan, TMT),
                    R.drawable.ic_flag_turkmenistan
            ),
            Country(Currency(R.string.currency_tunisia, TND), R.drawable.ic_flag_tunisia),
            Country(Currency(R.string.currency_tonga, TOP), R.drawable.ic_flag_tonga),
            Country(Currency(R.string.currency_turkey, TRY), R.drawable.ic_flag_turkey),
            Country(
                    Currency(R.string.currency_trinidad_and_tobago, TTD),
                    R.drawable.ic_flag_trinidad_and_tobago
            ),
            Country(Currency(R.string.currency_taiwan, TWD), R.drawable.ic_flag_taiwan),
            Country(Currency(R.string.currency_tanzania, TZS), R.drawable.ic_flag_tanzania),
            Country(Currency(R.string.currency_ukraine, UAH), R.drawable.ic_flag_ukraine),
            Country(Currency(R.string.currency_uganda, UGX), R.drawable.ic_flag_uganda),
            Country(
                    Currency(R.string.currency_united_states_of_america, USD),
                    R.drawable.ic_flag_united_states_of_america
            ),
            Country(Currency(R.string.currency_uruguay, UYU), R.drawable.ic_flag_uruguay),
            Country(Currency(R.string.currency_uzbekistan, UZS), R.drawable.ic_flag_uzbekistan),
            Country(Currency(R.string.currency_venezuela, VES), R.drawable.ic_flag_venezuela),
            Country(Currency(R.string.currency_vietnam, VND), R.drawable.ic_flag_vietnam),
            Country(Currency(R.string.currency_vanuatu, VUV), R.drawable.ic_flag_vanuatu),
            Country(Currency(R.string.currency_samoa, WST), R.drawable.ic_flag_samoa),
            Country(Currency(R.string.currency_silver_ounce, XAG), R.drawable.ic_silver_ounce),
            Country(Currency(R.string.currency_gold_ounce, XAU), R.drawable.ic_gold_ounce),
            Country(
                    Currency(R.string.currency_palladium_ounce, XPD),
                    R.drawable.ic_palladium_ounce
            ),
            Country(
                    Currency(R.string.currency_platinum_ounce, XPT),
                    R.drawable.ic_platinum_ounce
            ),
            Country(Currency(R.string.currency_yemen, YER), R.drawable.ic_flag_yemen),
            Country(Currency(R.string.currency_south_africa, ZAR), R.drawable.ic_flag_south_africa),
            Country(Currency(R.string.currency_zambia, ZMW), R.drawable.ic_flag_zambia),
            Country(Currency(R.string.currency_zimbabwe, ZWL), R.drawable.ic_flag_zimbabwe),
    )
}