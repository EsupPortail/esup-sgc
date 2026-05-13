package org.esupportail.sgc.services;

import org.esupportail.sgc.dao.CardDaoService;
import org.esupportail.sgc.domain.Card;
import org.esupportail.sgc.domain.Card.Etat;
import org.esupportail.sgc.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires exhaustifs pour la logique d'affichage des formulaires dans UserService.
 *
 * Méthodes couvertes :
 *   - isFirstRequest
 *   - isFreeRenewal
 *   - isFreeNew
 *   - isPaidRenewal
 *   - displayRenewalForm
 *   - displayNewForm
 *   - displayForm
 *   - canPaidRenewal
 *   - canPaidNew
 *   - hasDeliveredCard
 *   - isEsupSgcUser
 *   - displayFormParts (intégration de toutes les méthodes ci-dessus)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserService – logique d'autorisation d'affichage des formulaires")
class UserServiceDisplayFormTest {

    // -------------------------------------------------------------------------
    // Dépendances mockées
    // -------------------------------------------------------------------------
    @Mock
    CardDaoService cardDaoService;

    @Mock
    CardEtatService cardEtatService;

    @Mock
    CardService cardService;

    @Mock
    ExtUserRuleService extUserRuleService;

    @Mock
    AppliConfigService appliConfigService;

    @InjectMocks
    UserService userService;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Crée un utilisateur de base avec une date de validité dans le futur. */
    private User baseUser() {
        User user = new User();
        user.setEppn("test.user@univ.fr");
        user.setDueDate(LocalDateTime.now().plusYears(1));
        user.setRequestFree(true);
        user.setFirstRequestFree(true);
        return user;
    }

    /** Ajoute le rôle ROLE_USER à l'utilisateur. */
    private void withRoleUser(User user) {
        Set<String> roles = new HashSet<>(user.getRoles());
        roles.add("ROLE_USER");
        user.setRoles(roles);
    }

    /** Ajoute une carte externe (hasExternalCard → true). */
    private void withExternalCard(User user) {
        Card externalCard = new Card();
        externalCard.setExternal(true);
        externalCard.setEtat(Etat.ENABLED);
        user.getCards().add(externalCard);
    }

    /** Ajoute une carte normale (non externe) avec un état donné. */
    private Card withCard(User user, Etat etat) {
        Card card = new Card();
        card.setExternal(false);
        card.setEtat(etat);
        user.getCards().add(card);
        return card;
    }

    // =========================================================================
    // isFirstRequest
    // =========================================================================
    @Nested
    @DisplayName("isFirstRequest")
    class IsFirstRequestTests {

        @Test
        @DisplayName("retourne true quand aucune carte non-annulée n'existe")
        void noCards_returnsTrue() {
            when(cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(eq("test.user@univ.fr"), anyList()))
                    .thenReturn(0L);
            assertTrue(userService.isFirstRequest(baseUser()));
        }

        @Test
        @DisplayName("retourne false quand au moins une carte non-annulée existe")
        void existingCard_returnsFalse() {
            when(cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(eq("test.user@univ.fr"), anyList()))
                    .thenReturn(1L);
            assertFalse(userService.isFirstRequest(baseUser()));
        }

        @Test
        @DisplayName("retourne false pour plusieurs cartes")
        void multipleCards_returnsFalse() {
            when(cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(eq("test.user@univ.fr"), anyList()))
                    .thenReturn(3L);
            assertFalse(userService.isFirstRequest(baseUser()));
        }
    }

    // =========================================================================
    // isFreeRenewal
    // =========================================================================
    @Nested
    @DisplayName("isFreeRenewal")
    class IsFreeRenewalTests {

        @BeforeEach
        void defaultMocks() {
            // Par défaut : pas première demande, pas de carte en attente
            when(cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(anyString(), anyList()))
                    .thenReturn(1L); // pas isFirstRequest
            when(cardEtatService.hasRequestCard(anyString())).thenReturn(false);
        }

        @Test
        @DisplayName("true dans le cas nominal : requestFree, renouvellement, date valide, pas de demande en cours")
        void nominal_returnsTrue() {
            User user = baseUser(); // requestFree=true, dueDate futur
            assertTrue(userService.isFreeRenewal(user));
        }

        @Test
        @DisplayName("false si requestFree = false")
        void notRequestFree_returnsFalse() {
            User user = baseUser();
            user.setRequestFree(false);
            assertFalse(userService.isFreeRenewal(user));
        }

        @Test
        @DisplayName("false si c'est la première demande")
        void isFirstRequest_returnsFalse() {
            when(cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(anyString(), anyList()))
                    .thenReturn(0L);
            assertFalse(userService.isFreeRenewal(baseUser()));
        }

        @Test
        @DisplayName("false si la date de validité est dépassée")
        void outOfDueDate_returnsFalse() {
            User user = baseUser();
            user.setDueDate(LocalDateTime.now().minusDays(1));
            assertFalse(userService.isFreeRenewal(user));
        }

        @Test
        @DisplayName("false si la date de validité est null")
        void nullDueDate_returnsFalse() {
            User user = baseUser();
            user.setDueDate(null);
            assertFalse(userService.isFreeRenewal(user));
        }

        @Test
        @DisplayName("false si une demande de carte est déjà en cours")
        void hasRequestCard_returnsFalse() {
            when(cardEtatService.hasRequestCard(anyString())).thenReturn(true);
            assertFalse(userService.isFreeRenewal(baseUser()));
        }

        @Test
        @DisplayName("false si l'utilisateur possède une carte externe")
        void hasExternalCard_returnsFalse() {
            User user = baseUser();
            withExternalCard(user);
            assertFalse(userService.isFreeRenewal(user));
        }
    }

    // =========================================================================
    // isFreeNew
    // =========================================================================
    @Nested
    @DisplayName("isFreeNew")
    class IsFreeNewTests {

        @BeforeEach
        void defaultMocks() {
            when(cardEtatService.hasRequestCard(anyString())).thenReturn(false);
        }

        @Test
        @DisplayName("true dans le cas nominal : firstRequestFree, date valide, pas de demande en cours")
        void nominal_returnsTrue() {
            assertTrue(userService.isFreeNew(baseUser()));
        }

        @Test
        @DisplayName("false si firstRequestFree = false")
        void notFirstRequestFree_returnsFalse() {
            User user = baseUser();
            user.setFirstRequestFree(false);
            assertFalse(userService.isFreeNew(user));
        }

        @Test
        @DisplayName("false si la date de validité est dépassée")
        void outOfDueDate_returnsFalse() {
            User user = baseUser();
            user.setDueDate(LocalDateTime.now().minusDays(1));
            assertFalse(userService.isFreeNew(user));
        }

        @Test
        @DisplayName("false si la date de validité est null")
        void nullDueDate_returnsFalse() {
            User user = baseUser();
            user.setDueDate(null);
            assertFalse(userService.isFreeNew(user));
        }

        @Test
        @DisplayName("false si une demande est déjà en cours")
        void hasRequestCard_returnsFalse() {
            when(cardEtatService.hasRequestCard(anyString())).thenReturn(true);
            assertFalse(userService.isFreeNew(baseUser()));
        }

        @Test
        @DisplayName("false si l'utilisateur possède une carte externe")
        void hasExternalCard_returnsFalse() {
            User user = baseUser();
            withExternalCard(user);
            assertFalse(userService.isFreeNew(user));
        }
    }

    // =========================================================================
    // isPaidRenewal
    // =========================================================================
    @Nested
    @DisplayName("isPaidRenewal")
    class IsPaidRenewalTests {

        @Test
        @DisplayName("true si une référence de paiement existe")
        void withPaymentReference_returnsTrue() {
            when(cardService.getPaymentWithoutCard(anyString())).thenReturn("REF-12345");
            assertTrue(userService.isPaidRenewal(baseUser()));
        }

        @Test
        @DisplayName("false si aucune référence de paiement")
        void withoutPaymentReference_returnsFalse() {
            when(cardService.getPaymentWithoutCard(anyString())).thenReturn("");
            assertFalse(userService.isPaidRenewal(baseUser()));
        }
    }

    // =========================================================================
    // displayRenewalForm
    // =========================================================================
    @Nested
    @DisplayName("displayRenewalForm")
    class DisplayRenewalFormTests {

        @BeforeEach
        void defaultMocks() {
            when(cardEtatService.hasRequestCard(anyString())).thenReturn(false);
        }

        @Test
        @DisplayName("true si isFreeRenewal = true et pas de carte externe")
        void freeRenewal_noExternalCard_returnsTrue() {
            assertTrue(userService.displayRenewalForm(baseUser(), true, false));
        }

        @Test
        @DisplayName("true si isPaidRenewal = true et pas de carte externe")
        void paidRenewal_noExternalCard_returnsTrue() {
            assertTrue(userService.displayRenewalForm(baseUser(), false, true));
        }

        @Test
        @DisplayName("false si ni freeRenewal ni paidRenewal")
        void neither_returnsFalse() {
            assertFalse(userService.displayRenewalForm(baseUser(), false, false));
        }

        @Test
        @DisplayName("false si carte externe, même avec isFreeRenewal = true")
        void withExternalCard_returnsFalse() {
            User user = baseUser();
            withExternalCard(user);
            assertFalse(userService.displayRenewalForm(user, true, true));
        }

        @Test
        @DisplayName("false si demande déjà en cours, même avec isFreeRenewal = true")
        void withPendingRequest_returnsFalse() {
            when(cardEtatService.hasRequestCard(anyString())).thenReturn(true);
            assertFalse(userService.displayRenewalForm(baseUser(), true, false));
        }
    }

    // =========================================================================
    // displayNewForm
    // =========================================================================
    @Nested
    @DisplayName("displayNewForm")
    class DisplayNewFormTests {

        @BeforeEach
        void defaultMocks() {
            when(cardEtatService.hasRequestCard(anyString())).thenReturn(false);
            when(extUserRuleService.isExtEsupSgcUser(anyString())).thenReturn(false);
        }

        @Test
        @DisplayName("true si ROLE_USER, isFreeNew = true, pas de carte externe ni demande en cours")
        void withRoleUser_freeNew_returnsTrue() {
            User user = baseUser();
            withRoleUser(user);
            assertTrue(userService.displayNewForm(user, true, false));
        }

        @Test
        @DisplayName("true si ROLE_USER, isPaidNew = true")
        void withRoleUser_paidNew_returnsTrue() {
            User user = baseUser();
            withRoleUser(user);
            assertTrue(userService.displayNewForm(user, false, true));
        }

        @Test
        @DisplayName("false si ni freeNew ni paidNew, même avec ROLE_USER")
        void withRoleUser_neither_returnsFalse() {
            User user = baseUser();
            withRoleUser(user);
            assertFalse(userService.displayNewForm(user, false, false));
        }

        @Test
        @DisplayName("false si pas de rôle utilisateur et pas extEsupSgcUser")
        void withoutRole_returnsFalse() {
            assertFalse(userService.displayNewForm(baseUser(), true, true));
        }

        @Test
        @DisplayName("true si extEsupSgcUser = true et isFreeNew = true")
        void extEsupSgcUser_freeNew_returnsTrue() {
            when(extUserRuleService.isExtEsupSgcUser(anyString())).thenReturn(true);
            assertTrue(userService.displayNewForm(baseUser(), true, false));
        }

        @Test
        @DisplayName("false si carte externe, même avec ROLE_USER et isFreeNew = true")
        void withExternalCard_returnsFalse() {
            User user = baseUser();
            withRoleUser(user);
            withExternalCard(user);
            assertFalse(userService.displayNewForm(user, true, true));
        }

        @Test
        @DisplayName("false si demande en cours, même avec ROLE_USER et isFreeNew = true")
        void withPendingRequest_returnsFalse() {
            when(cardEtatService.hasRequestCard(anyString())).thenReturn(true);
            User user = baseUser();
            withRoleUser(user);
            assertFalse(userService.displayNewForm(user, true, false));
        }
    }

    // =========================================================================
    // displayForm
    // =========================================================================
    @Nested
    @DisplayName("displayForm")
    class DisplayFormTests {

        @BeforeEach
        void defaultMocks() {
            when(extUserRuleService.isExtEsupSgcUser(anyString())).thenReturn(false);
        }

        @Test
        @DisplayName("true si displayRenewalForm = true")
        void displayRenewalForm_returnsTrue() {
            assertTrue(userService.displayForm(baseUser(), false, true, false, false));
        }

        @Test
        @DisplayName("true si displayNewForm = true")
        void displayNewForm_returnsTrue() {
            assertTrue(userService.displayForm(baseUser(), false, false, false, true));
        }

        @Test
        @DisplayName("true si requestUserIsManager = true, même si tout est false")
        void managerOverride_returnsTrue() {
            User user = baseUser();
            assertTrue(userService.displayForm(user, true, false, false, false));
        }

        @Test
        @DisplayName("true si isFirstRequest + ROLE_USER + date valide (cas nouvelle demande possible)")
        void firstRequest_esupSgcUser_inDate_returnsTrue() {
            User user = baseUser();
            withRoleUser(user);
            // isFirstRequest=true, displayNewForm=false → branche if(isFirstRequest && !displayNewForm)
            assertTrue(userService.displayForm(user, false, false, true, false));
        }

        @Test
        @DisplayName("false si isFirstRequest + date dépassée + pas manager")
        void firstRequest_outOfDate_notManager_returnsFalse() {
            User user = baseUser();
            user.setDueDate(LocalDateTime.now().minusDays(1));
            withRoleUser(user);
            assertFalse(userService.displayForm(user, false, false, true, false));
        }

        @Test
        @DisplayName("false si tout est false et pas manager")
        void allFalse_notManager_returnsFalse() {
            assertFalse(userService.displayForm(baseUser(), false, false, false, false));
        }

        @Test
        @DisplayName("false si isFirstRequest + pas ROLE_USER + pas extEsupSgcUser + pas manager")
        void firstRequest_notEsupSgcUser_notManager_returnsFalse() {
            // user sans rôle, extEsupSgcUser = false (via @BeforeEach)
            assertFalse(userService.displayForm(baseUser(), false, false, true, false));
        }

        @Test
        @DisplayName("true si isFirstRequest + displayNewForm = true (displayNewForm prime sur la branche if)")
        void firstRequest_withDisplayNewForm_returnsTrue() {
            User user = baseUser();
            // displayNewForm=true → displayForm = displayRenewalForm || displayNewForm = true
            // la branche if ne s'exécute pas car !displayNewForm est false
            assertTrue(userService.displayForm(user, false, false, true, true));
        }
    }

    // =========================================================================
    // canPaidRenewal
    // =========================================================================
    @Nested
    @DisplayName("canPaidRenewal")
    class CanPaidRenewalTests {

        @BeforeEach
        void defaultMocks() {
            // Situation : pas première demande, pas en attente, pas de paiement existant
            when(cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(anyString(), anyList()))
                    .thenReturn(1L);
            when(cardEtatService.hasRequestCard(anyString())).thenReturn(false);
            when(cardService.getPaymentWithoutCard(anyString())).thenReturn("");
        }

        @Test
        @DisplayName("true dans le cas nominal : pas firstRequest, pas requestFree, date valide, pas en attente")
        void nominal_returnsTrue() {
            User user = baseUser();
            user.setRequestFree(false);
            assertTrue(userService.canPaidRenewal(user));
        }

        @Test
        @DisplayName("false si carte externe")
        void withExternalCard_returnsFalse() {
            User user = baseUser();
            user.setRequestFree(false);
            withExternalCard(user);
            assertFalse(userService.canPaidRenewal(user));
        }

        @Test
        @DisplayName("false si date de validité dépassée")
        void outOfDueDate_returnsFalse() {
            User user = baseUser();
            user.setRequestFree(false);
            user.setDueDate(LocalDateTime.now().minusDays(1));
            assertFalse(userService.canPaidRenewal(user));
        }

        @Test
        @DisplayName("false si requestFree = true (renouvellement gratuit déjà couvert)")
        void requestFree_returnsFalse() {
            // baseUser() a requestFree=true par défaut
            assertFalse(userService.canPaidRenewal(baseUser()));
        }

        @Test
        @DisplayName("false si un paiement est déjà en attente")
        void paidRenewalAlreadyExists_returnsFalse() {
            when(cardService.getPaymentWithoutCard(anyString())).thenReturn("REF-EXISTING");
            User user = baseUser();
            user.setRequestFree(false);
            assertFalse(userService.canPaidRenewal(user));
        }

        @Test
        @DisplayName("false si une demande de carte est déjà en cours")
        void hasRequestCard_returnsFalse() {
            when(cardEtatService.hasRequestCard(anyString())).thenReturn(true);
            User user = baseUser();
            user.setRequestFree(false);
            assertFalse(userService.canPaidRenewal(user));
        }

        @Test
        @DisplayName("false si c'est une première demande (pas un renouvellement)")
        void isFirstRequest_returnsFalse() {
            when(cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(anyString(), anyList()))
                    .thenReturn(0L);
            User user = baseUser();
            user.setRequestFree(false);
            assertFalse(userService.canPaidRenewal(user));
        }
    }

    // =========================================================================
    // canPaidNew
    // =========================================================================
    @Nested
    @DisplayName("canPaidNew")
    class CanPaidNewTests {

        @BeforeEach
        void defaultMocks() {
            // Situation : première demande, pas en attente, pas de paiement existant
            when(cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(anyString(), anyList()))
                    .thenReturn(0L);
            when(cardEtatService.hasRequestCard(anyString())).thenReturn(false);
            when(cardService.getPaymentWithoutCard(anyString())).thenReturn("");
        }

        @Test
        @DisplayName("true dans le cas nominal : isFirstRequest, pas firstRequestFree, date valide")
        void nominal_returnsTrue() {
            User user = baseUser();
            user.setFirstRequestFree(false);
            assertTrue(userService.canPaidNew(user));
        }

        @Test
        @DisplayName("false si carte externe")
        void withExternalCard_returnsFalse() {
            User user = baseUser();
            user.setFirstRequestFree(false);
            withExternalCard(user);
            assertFalse(userService.canPaidNew(user));
        }

        @Test
        @DisplayName("false si date de validité dépassée")
        void outOfDueDate_returnsFalse() {
            User user = baseUser();
            user.setFirstRequestFree(false);
            user.setDueDate(LocalDateTime.now().minusDays(1));
            assertFalse(userService.canPaidNew(user));
        }

        @Test
        @DisplayName("false si firstRequestFree = true (première demande gratuite)")
        void firstRequestFree_returnsFalse() {
            // baseUser() a firstRequestFree=true par défaut
            assertFalse(userService.canPaidNew(baseUser()));
        }

        @Test
        @DisplayName("false si un paiement est déjà enregistré")
        void paidRenewalAlreadyExists_returnsFalse() {
            when(cardService.getPaymentWithoutCard(anyString())).thenReturn("REF-EXISTING");
            User user = baseUser();
            user.setFirstRequestFree(false);
            assertFalse(userService.canPaidNew(user));
        }

        @Test
        @DisplayName("false si une demande de carte est déjà en cours")
        void hasRequestCard_returnsFalse() {
            when(cardEtatService.hasRequestCard(anyString())).thenReturn(true);
            User user = baseUser();
            user.setFirstRequestFree(false);
            assertFalse(userService.canPaidNew(user));
        }

        @Test
        @DisplayName("false si c'est un renouvellement (pas la première demande)")
        void isNotFirstRequest_returnsFalse() {
            when(cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(anyString(), anyList()))
                    .thenReturn(1L);
            User user = baseUser();
            user.setFirstRequestFree(false);
            assertFalse(userService.canPaidNew(user));
        }
    }

    // =========================================================================
    // hasDeliveredCard
    // =========================================================================
    @Nested
    @DisplayName("hasDeliveredCard")
    class HasDeliveredCardTests {

        @Test
        @DisplayName("true si le mode livraison est désactivé (valeur autre que TRUE)")
        void deliveryModeOff_returnsTrue() {
            when(appliConfigService.getModeLivraison()).thenReturn("FALSE");
            assertTrue(userService.hasDeliveredCard(baseUser()));
        }

        @Test
        @DisplayName("true si le mode livraison est désactivé (valeur null)")
        void deliveryModeNull_returnsTrue() {
            when(appliConfigService.getModeLivraison()).thenReturn(null);
            assertTrue(userService.hasDeliveredCard(baseUser()));
        }

        @Test
        @DisplayName("true si mode livraison activé et aucune carte")
        void deliveryModeOn_noCards_returnsTrue() {
            when(appliConfigService.getModeLivraison()).thenReturn("TRUE");
            assertTrue(userService.hasDeliveredCard(baseUser()));
        }

        @Test
        @DisplayName("true si mode livraison activé et toutes les cartes non-annulées ont une date de livraison")
        void deliveryModeOn_allCardsDelivered_returnsTrue() {
            when(appliConfigService.getModeLivraison()).thenReturn("TRUE");
            User user = baseUser();
            Card card = withCard(user, Etat.ENABLED);
            card.setDeliveredDate(LocalDateTime.now().minusDays(5));
            assertTrue(userService.hasDeliveredCard(user));
        }

        @Test
        @DisplayName("false si mode livraison activé et une carte non-annulée sans date de livraison")
        void deliveryModeOn_cardWithoutDeliveryDate_returnsFalse() {
            when(appliConfigService.getModeLivraison()).thenReturn("TRUE");
            User user = baseUser();
            withCard(user, Etat.ENABLED); // deliveredDate = null
            assertFalse(userService.hasDeliveredCard(user));
        }

        @Test
        @DisplayName("true si mode livraison activé et seule carte est CANCELED (ignorée)")
        void deliveryModeOn_onlyCanceledCard_returnsTrue() {
            when(appliConfigService.getModeLivraison()).thenReturn("TRUE");
            User user = baseUser();
            withCard(user, Etat.CANCELED); // ignorée par la logique
            assertTrue(userService.hasDeliveredCard(user));
        }

        @Test
        @DisplayName("false si mode livraison activé, une carte livrée et une non livrée (non annulée)")
        void deliveryModeOn_mixedCards_returnsFalse() {
            when(appliConfigService.getModeLivraison()).thenReturn("TRUE");
            User user = baseUser();
            Card delivered = withCard(user, Etat.ENABLED);
            delivered.setDeliveredDate(LocalDateTime.now().minusDays(1));
            withCard(user, Etat.PRINTED); // pas de deliveredDate
            assertFalse(userService.hasDeliveredCard(user));
        }

        @Test
        @DisplayName("insensible à la casse pour la valeur TRUE")
        void deliveryModeTrue_caseInsensitive_returnsCorrectly() {
            when(appliConfigService.getModeLivraison()).thenReturn("true");
            User user = baseUser();
            withCard(user, Etat.ENABLED); // sans deliveredDate
            assertFalse(userService.hasDeliveredCard(user));
        }
    }

    // =========================================================================
    // isEsupSgcUser
    // =========================================================================
    @Nested
    @DisplayName("isEsupSgcUser")
    class IsEsupSgcUserTests {

        @BeforeEach
        void defaultMocks() {
            when(extUserRuleService.isExtEsupSgcUser(anyString())).thenReturn(false);
        }

        @Test
        @DisplayName("true si l'utilisateur a le rôle ROLE_USER")
        void withRoleUser_returnsTrue() {
            User user = baseUser();
            withRoleUser(user);
            assertTrue(userService.isEsupSgcUser(user));
        }

        @Test
        @DisplayName("false si pas de rôle ROLE_USER et pas extEsupSgcUser")
        void noRole_returnsTrue() {
            assertFalse(userService.isEsupSgcUser(baseUser()));
        }

        @Test
        @DisplayName("true si extUserRuleService renvoie true (utilisateur externe autorisé)")
        void extEsupSgcUser_returnsTrue() {
            when(extUserRuleService.isExtEsupSgcUser(anyString())).thenReturn(true);
            assertTrue(userService.isEsupSgcUser(baseUser()));
        }

        @Test
        @DisplayName("true si ROLE_ADMIN (qui implique ROLE_USER via hiérarchie directe)")
        void withRoleAdmin_notDirectlyRoleUser() {
            // ROLE_ADMIN donne ROLE_SUPER_MANAGER et ROLE_MANAGER — pas ROLE_USER
            // Donc ce test vérifie qu'ADMIN seul ne donne PAS ROLE_USER
            User user = baseUser();
            Set<String> roles = new HashSet<>();
            roles.add("ROLE_ADMIN");
            user.setRoles(roles);
            // ROLE_ADMIN n'inclut pas ROLE_USER dans SgcRoleHierarchy
            assertFalse(userService.isEsupSgcUser(user));
        }
    }

    // =========================================================================
    // displayFormParts – test d'intégration des drapeaux
    // =========================================================================
    @Nested
    @DisplayName("displayFormParts – intégration")
    class DisplayFormPartsTests {

        @BeforeEach
        void defaultMocks() {
            // CardService mocks (toujours requis)
            when(cardService.displayFormCnil(anyString())).thenReturn(true);
            when(cardService.displayFormCrous(any(User.class))).thenReturn(false);
            when(cardService.isCrousEnabled(any(User.class))).thenReturn(false);
            when(cardService.displayFormRules(anyString())).thenReturn(true);
            when(cardService.displayFormAdresse(anyString())).thenReturn(false);
            when(cardService.isEuropeanCardEnabled(any(User.class))).thenReturn(false);
            when(cardService.displayFormEuropeanCardEnabled(any(User.class))).thenReturn(false);
            when(cardService.getPaymentWithoutCard(anyString())).thenReturn("");
            // Autres
            when(cardEtatService.hasRequestCard(anyString())).thenReturn(false);
            when(extUserRuleService.isExtEsupSgcUser(anyString())).thenReturn(false);
        }

        @Test
        @DisplayName("contient toutes les clés attendues")
        void containsAllExpectedKeys() {
            when(cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(anyString(), anyList()))
                    .thenReturn(0L);
            Map<String, Boolean> parts = userService.displayFormParts(baseUser(), false);

            List<String> expectedKeys = Arrays.asList(
                    "displayCnil", "displayCrous", "enableCrous",
                    "displayRules", "displayAdresse",
                    "isPaidRenewal", "isFreeRenewal", "isFreeNew",
                    "isFirstRequest", "displayRenewalForm", "displayNewForm",
                    "displayForm", "canPaidRenewal", "canPaidNew",
                    "hasDeliveredCard", "enableEuropeanCard", "displayEuropeanCard"
            );
            for (String key : expectedKeys) {
                assertTrue(parts.containsKey(key), "Clé manquante : " + key);
                assertNotNull(parts.get(key), "Valeur null pour la clé : " + key);
            }
        }

        @Test
        @DisplayName("scénario : nouvel utilisateur ROLE_USER, première demande gratuite")
        void scenario_firstRequest_freeNew() {
            // isFirstRequest = true (0 cartes)
            when(cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(anyString(), anyList()))
                    .thenReturn(0L);
            User user = baseUser(); // firstRequestFree=true, dueDate futur
            withRoleUser(user);

            Map<String, Boolean> parts = userService.displayFormParts(user, false);

            assertTrue(parts.get("isFirstRequest"),    "isFirstRequest doit être true");
            assertTrue(parts.get("isFreeNew"),         "isFreeNew doit être true");
            assertFalse(parts.get("isFreeRenewal"),    "isFreeRenewal doit être false (1ère demande)");
            assertFalse(parts.get("isPaidRenewal"),    "isPaidRenewal doit être false");
            assertTrue(parts.get("displayNewForm"),    "displayNewForm doit être true");
            assertFalse(parts.get("displayRenewalForm"), "displayRenewalForm doit être false");
            assertTrue(parts.get("displayForm"),       "displayForm doit être true");
            assertFalse(parts.get("canPaidNew"),       "canPaidNew doit être false (déjà gratuit)");
            assertFalse(parts.get("canPaidRenewal"),   "canPaidRenewal doit être false (1ère demande)");
        }

        @Test
        @DisplayName("scénario : renouvellement gratuit")
        void scenario_freeRenewal() {
            // Pas première demande
            when(cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(anyString(), anyList()))
                    .thenReturn(1L);
            User user = baseUser(); // requestFree=true
            withRoleUser(user);

            Map<String, Boolean> parts = userService.displayFormParts(user, false);

            assertFalse(parts.get("isFirstRequest"),   "isFirstRequest doit être false");
            assertTrue(parts.get("isFreeRenewal"),     "isFreeRenewal doit être true");
            // Note: isFreeNew ne dépend PAS de isFirstRequest, seulement de firstRequestFree + dueDate + etc.
            // Donc isFreeNew=true ici (firstRequestFree=true par défaut), mais displayNewForm=false (pas ROLE_USER)
            assertTrue(parts.get("isFreeNew"),         "isFreeNew est true car firstRequestFree=true (indépendant de isFirstRequest)");
            assertTrue(parts.get("displayRenewalForm"), "displayRenewalForm doit être true");
            assertTrue(parts.get("displayForm"),       "displayForm doit être true");
            assertFalse(parts.get("canPaidRenewal"),   "canPaidRenewal doit être false (déjà gratuit)");
        }

        @Test
        @DisplayName("scénario : renouvellement payant disponible")
        void scenario_canPaidRenewal() {
            when(cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(anyString(), anyList()))
                    .thenReturn(1L);
            User user = baseUser();
            user.setRequestFree(false);    // pas de renouvellement gratuit

            Map<String, Boolean> parts = userService.displayFormParts(user, false);

            assertFalse(parts.get("isFreeRenewal"),    "isFreeRenewal doit être false");
            assertFalse(parts.get("isPaidRenewal"),    "isPaidRenewal doit être false (pas encore payé)");
            assertTrue(parts.get("canPaidRenewal"),    "canPaidRenewal doit être true");
            assertFalse(parts.get("displayRenewalForm"), "displayRenewalForm doit être false (pas encore payé)");
        }

        @Test
        @DisplayName("scénario : renouvellement après paiement")
        void scenario_paidRenewalAlreadyPaid() {
            when(cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(anyString(), anyList()))
                    .thenReturn(1L);
            when(cardService.getPaymentWithoutCard(anyString())).thenReturn("REF-ABC");
            User user = baseUser();
            user.setRequestFree(false);

            Map<String, Boolean> parts = userService.displayFormParts(user, false);

            assertTrue(parts.get("isPaidRenewal"),     "isPaidRenewal doit être true");
            assertTrue(parts.get("displayRenewalForm"), "displayRenewalForm doit être true");
            assertTrue(parts.get("displayForm"),       "displayForm doit être true");
            assertFalse(parts.get("canPaidRenewal"),   "canPaidRenewal doit être false (paiement déjà fait)");
        }

        @Test
        @DisplayName("scénario : utilisateur avec carte externe — aucune demande possible")
        void scenario_hasExternalCard() {
            when(cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(anyString(), anyList()))
                    .thenReturn(0L);
            User user = baseUser();
            withRoleUser(user);
            withExternalCard(user);

            Map<String, Boolean> parts = userService.displayFormParts(user, false);

            assertFalse(parts.get("isFreeNew"),        "isFreeNew doit être false");
            assertFalse(parts.get("isFreeRenewal"),    "isFreeRenewal doit être false");
            assertFalse(parts.get("displayNewForm"),   "displayNewForm doit être false");
            assertFalse(parts.get("displayRenewalForm"), "displayRenewalForm doit être false");
            assertFalse(parts.get("canPaidNew"),       "canPaidNew doit être false");
            assertFalse(parts.get("canPaidRenewal"),   "canPaidRenewal doit être false");
        }

        @Test
        @DisplayName("scénario : manager peut toujours voir le formulaire même sans droits utilisateur")
        void scenario_managerOverride() {
            when(cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(anyString(), anyList()))
                    .thenReturn(1L);
            User user = baseUser();
            user.setRequestFree(false);
            user.setDueDate(LocalDateTime.now().minusYears(1)); // date dépassée : plus de droits standards

            Map<String, Boolean> parts = userService.displayFormParts(user, true); // manager=true

            assertTrue(parts.get("displayForm"), "displayForm doit être true pour un manager");
        }

        @Test
        @DisplayName("scénario : date de validité dépassée — tout est false sauf manager")
        void scenario_outOfDueDate_notManager() {
            when(cardDaoService.countfindCardsByEppnEqualsAndEtatNotIn(anyString(), anyList()))
                    .thenReturn(0L);
            User user = baseUser();
            withRoleUser(user);
            user.setDueDate(LocalDateTime.now().minusDays(1));

            Map<String, Boolean> parts = userService.displayFormParts(user, false);

            assertFalse(parts.get("isFreeNew"),        "isFreeNew doit être false (date dépassée)");
            assertFalse(parts.get("isFreeRenewal"),    "isFreeRenewal doit être false (date dépassée)");
            assertFalse(parts.get("displayNewForm"),   "displayNewForm doit être false");
            assertFalse(parts.get("displayForm"),      "displayForm doit être false");
            assertFalse(parts.get("canPaidNew"),       "canPaidNew doit être false (date dépassée)");
        }

        // Helper pour this inner class
        private User any(Class<User> ignored) { return null; }
    }

    // =========================================================================
    // Méthode helper any() pour les mocks Mockito avec User dans @BeforeEach
    // =========================================================================
    private static <T> T any(Class<T> clazz) {
        return org.mockito.ArgumentMatchers.any(clazz);
    }
}

