package org.esupportail.sgc.tools;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.dao.CodeBarreDaoService;
import org.esupportail.sgc.dao.UserDaoService;
import org.springframework.stereotype.Service;

@Service
public class CodeBarreUtils {

    @Resource
    CodeBarreDaoService codeBarreDaoService;

    private static final String PREMIER_CHIFFRE_POUR_CODE_BARRE_BU_COMUE_LYON_ST_ETIENNE = "2";

    /**
     *
     * <h3>Code barre utilisé dans les bibliothèques (BU) de la COMUE de Lyon/Saint-Etienne</h3>
     * Un num&eacute;ro unique est inscrit dans le code barre.
     * <br/>
     * Ce num&eacute;ro est compos&eacute; de 14 chiffres d&eacute;coup&eacute;s en 3 segments.
     * <br/><br/>
     * <table>
     *  <tr>
     *   <td>Segment</td>
     *   <td>Signification</td>
     *   <td>D&eacute;composition</td>
     *  </tr>
     *  <tr>
     *   <td>1</td>
     *   <td>Identification de l'&eacute;tablissement</td>
     *   <td>
     *     1<sup>er</sup> chiffre = 2 (pour diff&eacute;rencier des livres dont le code barre commence par 3)
     *     <br/><br/>
     *     4 chiffres suivants = code d'&eacute;tablissement&nbsp;utilis&eacute; par les biblioth&egrave;ques
     *     <br/>
     *     ex: 6903 pour l'Universit&eacute; Lyon 3
     *   </td>
     *  </tr>
     *  <tr>
     *   <td>2</td>
     *   <td>Num&eacute;ro s&eacute;quentiel de 8 chiffres</td>
     *   <td>&nbsp;</td>
     *  </tr>
     *  <tr>
     *   <td>3</td>
     *   <td>Cl&eacute; de contr&ocirc;le</td>
     *   <td>1 chiffre</td>
     *  </tr>
     * </table>
     * <br/>
     * <table>
     *  <tr>
     *   <td>Etablissement</td>
     *   <td>Code utilis&eacute; par les biblioth&egrave;ques (BU)</td>
     *  </tr>
     *  <tr>
     *   <td>Universit&eacute; de Lyon</td>
     *   <td>6900</td>
     *  </tr>
     *  <tr>
     *   <td>Universit&eacute; Lyon 1</td>
     *   <td>6901</td>
     *  </tr>
     *  <tr>
     *   <td>Universit&eacute; Lyon 2</td>
     *   <td>6902</td>
     *  </tr>
     *  <tr>
     *   <td>Universit&eacute; Lyon 3</td>
     *   <td>6903</td>
     *  </tr>
     *  <tr>
     *   <td>Ecole normale sup&eacute;rieure de Lyon</td>
     *   <td>6904</td>
     *  </tr>
     *  <tr>
     *   <td>Ecole normale suprieure de Fontenay - Saint-Cloud</td>
     *   <td>6905</td>
     *  </tr>
     *   <tr>
     *   <td>Ecole centrale de Lyon</td>
     *   <td>6906</td>
     *  </tr>
     *  <tr>
     *   <td>Universit&eacute; Jean Monnet de Saint-Etienne</td>
     *   <td>6907</td>
     *  </tr>
     *  </tr>
     *  <tr>
     *   <td>Institut National des Sciences Appliqu&eacute;es de Lyon (INSA)</td>
     *   <td>6908</td>
     *  </tr>
     *  <tr>
     *   <td>Institut d'&eacute;tudes politiques de Lyon (IEP)</td>
     *   <td>6911</td>
     *  </tr>
     *  <tr>
     *   <td>Ecole V&eacute;t&eacute;rinaire de Lyon (ENVL)</td>
     *   <td>6912</td>
     *  </tr>
     *  <tr>
     *   <td>Ecole nationale des travaux publics de l'Etat (ENTPE)</td>
     *   <td>6913</td>
     *  </tr>
     *  <tr>
     *   <td colspan="2">Institut polytechnique de Lyon</td>
     *  </tr>
     *  <tr>
     *   <td>&nbsp;&nbsp;&nbsp;&nbsp;CPE</td>
     *   <td>6914</td>
     *  </tr>
     *  <tr>
     *   <td>&nbsp;&nbsp;&nbsp;&nbsp;ECAM</td>
     *   <td>6914</td>
     *  </tr>
     *  <tr>
     *   <td>&nbsp;&nbsp;&nbsp;&nbsp;ISARA</td>
     *   <td>6914</td>
     *  </tr>
     *  <tr>
     *   <td>&nbsp;&nbsp;&nbsp;&nbsp;ITECH</td>
     *   <td>6914</td>
     *  </tr>
     *  <tr>
     *   <td>&nbsp;&nbsp;&nbsp;&nbsp;IPL</td>
     *   <td>6914</td>
     *  </tr>
     *  <tr>
     *   <td>Institut Catholique de Lyon</td>
     *   <td>6915</td>
     *  </tr>
     *  <tr>
     *   <td>Ecole Nationale Sup&eacuterieur des Sciences de l'Information et des Biblioth&egrave;ques (ENSSIB)</td>
     *   <td>6916</td>
     *  </tr>
     *  <tr>
     *   <td>Ecole de Management de Lyon (EML)</td>
     *   <td>6917</td>
     *  </tr>
     *  <tr>
     *   <td>Ecole Nationale d'ing&eacute;nieurs de Saint-Etienne (ENISE)</td>
     *   <td>6918</td>
     *  </tr>
     *  <tr>
     *   <td>Ecole Nationale Sup&eacute;rieur d'Architecture de Lyon (ENSAL)</td>
     *   <td>6919</td>
     *  </tr>
     *  <tr>
     *   <td>Ecole Nationale des Arts et Techniques du Th&eacute;&acirc;tre (ENSATT)</td>
     *   <td>6920
     *   </td>
     *  </tr>
     *  <tr>
     *   <td>Institut National de la Recherche P&eacute;dagogique (INRP)</td>
     *   <td>6921</td>
     *  </tr>
     * </table>
     * <br/>
     * <h4>Algorithme de calcul de la cl&eacute;</h4>
     * Somme = 0
     * <br/>
     * Lire les chiffres du 1<sup>er</sup> au 13<sup>&egrave;me</sup>
     * <br/>
     * Si c'est un chiffre en position impaire (1<sup>er</sup> chiffre, 3<sup>&egrave;me</sup> etc.), la valeur du chiffre est multipli&eacute;e par 2. Si le r&eacute;sultat est sup&eacute;rieur &agrave; 9, on additionne les deux chiffres du r&eacute;sultat pour obtenir un seul chiffre.
     * <br/>
     * <i>Somme </i>augment&eacute;e de la valeur du chiffre
     * r&eacute;sultat.
     * <br/>
     * Si c'est un chiffre en position paire (2<sup>&egrave;me</sup>, 4<sup>&egrave;me</sup> etc.), <i>Somme</i> augment&eacute;e de la valeur du chiffre.
     * <br/>
     * Calculer le modulo de <i>Somme</i> par 10.
     * <br/>
     * Si le modulo = 0, la cl&eacute; = 0.
     * <br/>
     * Sinon la cl&eacute; = 10-modulo.
     * <br/><br/>
     * <u>Exemple</u> :
     * <br/>
     * Prenons la 23899<sup>i&egrave;me</sup> carte &eacute;tudiante de l'Universit&eacute; Lyon 3
     * <br/>
     * 1<sup>er</sup> segment = 26903
     * <br/>
     * 2<sup>i&egrave;me</sup> segment=00023899
     * <br/>
     * 3<sup>i&egrave;me</sup> segment = 3
     * <br/><br/>
     * <table>
     *  <tr>
     *   <td>Position</td>
     *   <td>Valeur</td>
     *   <td>Calcul</td>
     *   <td>R&eacute;sultat</td>
     *  </tr>
     *  <tr>
     *   <td>1</td>
     *   <td>2</td>
     *   <td>2x2=4</td>
     *   <td>4</td>
     *  </tr>
     *  <tr>
     *   <td>2</td>
     *   <td>6</td>
     *   <td>6</td>
     *   <td>6</td>
     *  </tr>
     *  <tr>
     *   <td>3</td>
     *   <td>9</td>
     *   <td>9x2=18 -&gt; 1+8=9</td>
     *   <td>9</td>
     *  </tr>
     *  <tr>
     *   <td>4</td>
     *   <td>0</td>
     *   <td>0</td>
     *   <td>0</td>
     *  </tr>
     *  <tr>
     *   <td>5</td>
     *   <td>3</td>
     *   <td>3x2=6</td>
     *   <td>6</td>
     *  </tr>
     *  <tr>
     *   <td>6</td>
     *   <td>0</td>
     *   <td>0</td>
     *   <td>0</td>
     *  </tr>
     *  <tr>
     *   <td>7</td>
     *   <td>0</td>
     *   <td>0x2=0</td>
     *   <td>0</td>
     *  </tr>
     *  <tr>
     *   <td>8</td>
     *   <td>0</td>
     *   <td>0</td>
     *   <td>0</td>
     *  </tr>
     *  <tr>
     *   <td>9</td>
     *   <td>2</td>
     *   <td>2x2=4</td>
     *   <td>4</td>
     *  </tr>
     *  <tr>
     *   <td>10</td>
     *   <td>3</td>
     *   <td>3</td>
     *   <td>3</td>
     *  </tr>
     *  <tr>
     *   <td>11</td>
     *   <td>8</td>
     *   <td>8x2=16 -&gt; 1+6=7</td>
     *   <td>7</td>
     *  </tr>
     *  <tr>
     *   <td>12</td>
     *   <td>9</td>
     *   <td>9</td>
     *   <td>9</td>
     *  </tr>
     *  <tr>
     *   <td>13</td>
     *   <td>9</td>
     *   <td>9x2=18 -&gt; 1+8=9</td>
     *   <td>9</td>
     *  </tr>
     *  <tr>
     *   <td colspan="3">Somme</td>
     *   <td>57</td>
     *  </tr>
     * </table>
     * <br/>
     * 57 modulo 10 = 7
     * <br/>
     * 10 - 7 = 3
     * <br/>
     * La cl&eacute; est donc <b>3</b>
     * <br/>
     * Code barre : 26903000238993
     * <br/>
     * Soit cod&eacute; en Code 39 (Police C39HrP24DhTt comme indiqu&eacute; dans la maquette) : <b>*26903000238993*</b>
     * <br/>
     * <u>Remarque</u> : Le code39 n&eacute;cessite l'adjonction d'un "*" comme caract&egrave;re de d&eacute;but et de fin de codage.
     *
     * @param pCodeBibliotheque              code d'établissement utilisé par les bibliothèques de la COMUE de Lyon/Saint-Etienne
     * @param pNbCartesImprimesDansAncienSgc nombre de cartes imprimées dans l'établissement avant la mise en place d'esup-sgc
     * @return chaîne de caractère représentant le num&eacute;ro unique inscrit dans le code barre
     */
    public String genereCodeBarreBuComueLyonStEtienne(Integer pCodeBibliotheque, Integer pNbCartesImprimesDansAncienSgc) {
        String lCodeBarre = null;
        if (pCodeBibliotheque != null && pNbCartesImprimesDansAncienSgc != null) {
            String lCodeBib = StringUtils.trim(pCodeBibliotheque.toString());
            if (StringUtils.isNotEmpty(lCodeBib) && StringUtils.isNotBlank(lCodeBib)) {
                int lNbCodeBarreBuComueLyonStEtienneGenereDansEsupSgcPlusUn = codeBarreDaoService.getNextValueCodeBarreBuLyonStEtienne();
                int lNbCarte = pNbCartesImprimesDansAncienSgc + lNbCodeBarreBuComueLyonStEtienneGenereDansEsupSgcPlusUn;
                String numeroCarteBU = PREMIER_CHIFFRE_POUR_CODE_BARRE_BU_COMUE_LYON_ST_ETIENNE + lCodeBib + StringUtils.leftPad(String.valueOf(lNbCarte), 8, '0');
                int somme = 0;
                int result = Integer.parseInt(numeroCarteBU.substring(0, 1)) * 2;
                if (result > 9) {
                    result = result / 10 + result % 10;
                }
                somme = result + Integer.parseInt(numeroCarteBU.substring(1, 2));

                result = Integer.parseInt(numeroCarteBU.substring(2, 3)) * 2;
                if (result > 9) {
                    result = result / 10 + result % 10;
                }
                somme = somme + result + Integer.parseInt(numeroCarteBU.substring(3, 4));

                result = Integer.parseInt(numeroCarteBU.substring(4, 5)) * 2;
                if (result > 9) {
                    result = result / 10 + result % 10;
                }
                somme = somme + result + Integer.parseInt(numeroCarteBU.substring(5, 6));

                result = Integer.parseInt(numeroCarteBU.substring(6, 7)) * 2;
                if (result > 9) {
                    result = result / 10 + result % 10;
                }
                somme = somme + result + Integer.parseInt(numeroCarteBU.substring(7, 8));

                result = Integer.parseInt(numeroCarteBU.substring(8, 9)) * 2;
                if (result > 9) {
                    result = result / 10 + result % 10;
                }
                somme = somme + result + Integer.parseInt(numeroCarteBU.substring(9, 10));

                result = Integer.parseInt(numeroCarteBU.substring(10, 11)) * 2;
                if (result > 9) {
                    result = result / 10 + result % 10;
                }
                somme = somme + result + Integer.parseInt(numeroCarteBU.substring(11, 12));

                result = Integer.parseInt(numeroCarteBU.substring(12)) * 2;
                if (result > 9) {
                    result = result / 10 + result % 10;
                }
                somme = somme + result;
                somme = somme % 10;
                if (somme > 0) {
                    somme = 10 - somme;
                }
                lCodeBarre = "*" + numeroCarteBU + Integer.toString(somme) + "*";
            }
        }
        return lCodeBarre;
    }
}
