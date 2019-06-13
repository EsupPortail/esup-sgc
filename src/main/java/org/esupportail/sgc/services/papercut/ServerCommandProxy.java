/*
 * (c) Copyright 1999-2012 PaperCut Software International Pty Ltd.
 */

package org.esupportail.sgc.services.papercut;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpc;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

/**
 * A proxy designed to wrap XML-RPC calls the Application Server's XML-RPC API commands. This class 
 * requires the Apache XML-RPC Library version 2.0.x.
 * 
 * This library can be obtained from the Application Server install directory.
 * See README.txt for details.
 */
public class ServerCommandProxy {
    private final XmlRpcClient _xmlRpcClient;
    private final String _authToken;

    /**
     * The constructor.
     * 
     * @param server The name or IP address of the server hosting the Application Server. The server should be
     *               configured to allow XML-RPC connections from the host running this proxy class. Localhost is
     *               generally accepted by default.
     * @param port The port the Application Server is listening on. This is port 9191 on a default install.
     * @param authToken The authentication token as a string. All RPC calls must pass through an authentication token.
     *                  At the current time this is simply the built-in "admin" user's password.
     */
    public ServerCommandProxy(String server, String scheme, int port, String authToken) {
        _authToken = authToken;
        // We want to use keep alives if possible.
        XmlRpc.setKeepAlive(true);
        String serverURL;
        try {
            serverURL = (new URL(scheme, server, port, "/rpc/api/xmlrpc")).toString();
            _xmlRpcClient = new XmlRpcClient(serverURL);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid server name supplied");
        }
    }

    /**
     * Test to see if a user associated with "username" exists in the system.
     * 
     * @param username The username to test.
     * @return Returns true if the user exists in the system, else returns false.
     */
    public boolean isUserExists(String username) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(username);
        Boolean r = (Boolean) execute("api.isUserExists", params);
        return r.booleanValue();
    }
    
    /**
     * The a user's current account balance.
     * 
     * If multiple personal accounts are enabled, this will return the total balance of all accounts.
     * 
     * @param username The user's username.
     * @return The user's current account balance as a double.
     */
    public double getUserAccountBalance(String username) {
        return getUserAccountBalance(username, "");
    }
    
    /**
     * The a user's current account balance.
     * 
     * @param username The user's username.
     * @param accountName Optional name of the user's personal account. If blank, the total user balance is returned.
     * @return The user's current account balance as a double.
     */
    public double getUserAccountBalance(String username, String accountName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(username);
        params.add(trimToEmpty(accountName));
        
        Double r = (Double) execute("api.getUserAccountBalance", params);
        return r.doubleValue();
    }

    /**
     * Gets a user property.
     *
     * @param userName The name of the user.
     * @param propertyName The name of the property to get.  The following list of property names can also be set using
     * {@link #setUserProperty #setUserProperty}:
     *<ul>
     * <li>{@code balance}: the user's balance, unformatted, e.g. "1234.56".</li>
     * <li>{@code card-number}</li>
     * <li>{@code card-number2}</li>
     * <li>{@code card-pin}</li>
     * <li>{@code department}</li>
     * <li>{@code disabled-net}: {@code true} if the user's 'net access is disabled, otherwise {@code false}</li>
     * <li>{@code disabled-print}: {@code true} if the user's printing is disabled, otherwise {@code false}</li>
     * <li>{@code email}</li>
     * <li>{@code full-name}</li>
     * <li>{@code internal}: {@code true} if this is an internal user, otherwise {@code false}</li>
     * <li>{@code notes}</li>
     * <li>{@code office}</li>
     * <li>{@code print-stats.job-count}: total number of print jobs from this user, unformatted, e.g. "1234"</li>
     * <li>{@code print-stats.page-count}: total number of pages printed by this user, unformatted, e.g. "1234"</li>
     * <li>{@code net-stats.data-mb}: total 'net MB used by this user, unformatted, e.g. "1234.56"</li>
     * <li>{@code net-stats.time-hours}: total 'net hours used by this user, unformatted, e.g. "1234.56"</li>
     * <li>
     *  {@code restricted}: {@code true} if this user's printing is restricted, {@code false} if they are unrestricted.
     * </li>
     *</ul>
     * The following options are "read only", i.e. cannot be set using {@link #setUserProperty #setUserProperty}:
     *<ul>
     * <li>
     *  {@code account-selection.mode}: the user's account selection mode.  One of the following:
     *  <ul>
     *   <li>{@code AUTO_CHARGE_TO_PERSONAL_ACCOUNT}</li>
     *   <li>{@code CHARGE_TO_PERSONAL_ACCOUNT_WITH_CONFIRMATION}</li>
     *   <li>{@code AUTO_CHARGE_TO_SHARED}</li>
     *   <li>{@code SHOW_ACCOUNT_SELECTION_POPUP}</li>
     *   <li>{@code SHOW_ADVANCED_ACCOUNT_SELECTION_POPUP}</li>
     *   <li>{@code SHOW_MANAGER_MODE_POPUP}</li>
     *  </ul>
     * </li>
     * <li>
     *  {@code account-selection.can-charge-personal}: {@code true} if the user's account selection settings allow them
     *  to charge jobs to their personal account, otherwise {@code false}.
     * </li>
     * <li>
     *  {@code account-selection.can-charge-shared-from-list}: {@code true} if the user's account selection settings
     *  allow them to select a shared account to charge from a list of shared accounts, otherwise {@code false}.
     * </li>
     * <li>
     *  {@code account-selection.can-charge-shared-by-pin}: {@code true} if the user's account selection settings allow
     *  them to charge a shared account by PIN or code, otherwise {@code false}.
     * </li>
     *</ul>
     * @return The value of the requested property.
     *
     * @see #setUserProperty
     */
    public String getUserProperty(String userName, String propertyName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(userName);
        params.add(propertyName);
        return (String) execute("api.getUserProperty", params);
    }

    /**
     * Get multiple user properties at once (to save multiple calls).
     *
     * @param userName The name of the user.
     * @param propertyNames The names of the properties to get.  See
     * {@link #getUserProperty #getUserProperty} for valid property names.
     * @return The property values (in the same order as given in {@code propertyNames}.
     *
     * @see #getUserProperty
     * @see #setUserProperties
     */
    @SuppressWarnings("unchecked")
    public Vector<String> getUserProperties(String userName, Vector<String> propertyNames) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(userName);
        params.add(propertyNames);
        return (Vector<String>) execute("api.getUserProperties", params);
    }

    /**
     * Sets a user property.
     *
     * @param userName The name of the user.
     * @param propertyName The name of the property to set.  All properties in
     * {@link #getUserProperty #getUserProperty} may be set in addition to the following: password.
     * @param propertyValue The value of the property to set.
     *
     * @see #getUserProperty
     */
    public void setUserProperty(String userName, String propertyName, String propertyValue) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(userName);
        params.add(propertyName);
        params.add(propertyValue);
        execute("api.setUserProperty", params);
    }

    /**
     * Set multiple user properties at once (to save multiple calls).
     *
     * @param userName The name of the user.
     * @param propertyNamesAndValues The list of property names and values to set.
     * E.g. [["balance", "1.20"], ["office", "East Wing"]].  See
     * {@link #setUserProperty #setUserProperty} for valid property names.
     *
     * @see #getUserProperties
     * @see #setUserProperty
     */
    public void setUserProperties(String userName, Vector<Vector<String>> propertyNamesAndValues) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(userName);
        params.add(propertyNamesAndValues);
        execute("api.setUserProperties", params);
    }

    /**
     * Adjust a user's built-in/default account balance by an adjustment amount. An adjustment may be positive 
     * (add to the user's account) or negative (subtract from the account).
     * 
     * @param username The username associated with the user who's account is to be adjusted.
     * @param adjustment The adjustment amount. Positive to add credit and negative to subtract.
     * @param comment A user defined comment to be associated with the transaction. This may be a null string.
     */
    public void adjustUserAccountBalance(String username, double adjustment, String comment) {
        adjustUserAccountBalance(username, adjustment, comment, "");
    }

    /**
     * Adjust a user's account balance by an adjustment amount. An adjustment may be positive 
     * (add to the user's account) or negative (subtract from the account).
     * 
     * @param username The username associated with the user who's account is to be adjusted.
     * @param adjustment The adjustment amount. Positive to add credit and negative to subtract.
     * @param comment A user defined comment to be associated with the transaction. This may be a null string.
     * @param accountName Optional name of the user's personal account. If blank, the built-in default account is used.
     *                      If multiple personal accounts is enabled the account name must be provided.
     */
    public void adjustUserAccountBalance(String username, double adjustment, String comment, String accountName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(username);
        params.add(adjustment);
        params.add(trimToEmpty(comment));
        params.add(trimToEmpty(accountName));
        execute("api.adjustUserAccountBalance", params);
    }
    
    /**
     *  Adjust a user's account balance. User lookup is by card number.
     *  An adjustment may be positive (add to the user's account), or negative (subtract from the account).
     *  
     * @param cardNumber The card number associated with the user who's account is to be adjusted.
     * @param adjustment The adjustment amount. Positive to add credit and negative to subtract.
     * @param comment A user defined comment to be associated with the transaction.  This may be a null string.
     * @return TRUE if successful, FALSE if not (eg. no users found for the supplied card number).
     */
    public boolean adjustUserAccountBalanceByCardNumber(String cardNumber, double adjustment, String comment) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(cardNumber);
        params.add(adjustment);
        params.add(comment);
        return ((Boolean) execute("api.adjustUserAccountBalanceByCardNumber", params));
    }
    
    /**
     * Adjust a user's account balance. User lookup is by card number.
     * An adjustment may be positive (add to the user's account), or negative (subtract from the account).
     * @param cardNumber The card number associated with the user who's account is to be adjusted.
     * @param adjustment The adjustment amount. Positive to add credit and negative to subtract.
     * @param comment A user defined comment to be associated with the transaction.  This may be a null string.
     * @param accountName Optional name of the user's personal account. If blank, the built-in default account is used.
     *                          If multiple personal accounts is enabled the account name must be provided.
     * @return TRUE if successful, FALSE if not (eg.  no users found for the supplied card number).
     */
    public boolean adjustUserAccountBalanceByCardNumber(String cardNumber, double adjustment, String comment,
                                                        String accountName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(cardNumber);
        params.add(adjustment);
        params.add(comment);
        params.add(accountName);
        return ((Boolean) execute("api.adjustUserAccountBalanceByCardNumber", params));        
    }
    
    /**
     * Adjust a user's account balance by an adjustment amount (if there is credit available).   This can be used
     * to perform atomic account adjustments, without need to check the user's balance first.
     * <p>
     * An adjustment may be positive (add to the user's account) or negative (subtract from the account).
     * 
     * @param username The username associated with the user who's account is to be adjusted.
     * @param adjustment The adjustment amount. Positive to add credit and negative to subtract.
     * @param comment A user defined comment to be associated with the transaction. This may be a null string.
     * @return Returns true if the adjustment was allowed. Returns false if the user didn't have enough available
     *          credit.
     */
    public boolean adjustUserAccountBalanceIfAvailable(String username, double adjustment, String comment) {
        return adjustUserAccountBalanceIfAvailable(username, adjustment, comment, "");
    }

    /**
     * Adjust a user's account balance by an adjustment amount (if there is credit available).   This can be used
     * to perform atomic account adjustments, without need to check the user's balance first.
     * <p>
     * An adjustment may be positive (add to the user's account) or negative (subtract from the account).
     * 
     * @param username The username associated with the user who's account is to be adjusted.
     * @param adjustment The adjustment amount. Positive to add credit and negative to subtract.
     * @param comment A user defined comment to be associated with the transaction. This may be a null string.
     * @param accountName Optional name of the user's personal account. If blank, the built-in default account is used.
     *                      If multiple personal accounts is enabled the account name must be provided.
     * @return Returns true if the adjustment was allowed. Returns false if the user didn't have enough available
     *          credit.
     */
    public boolean adjustUserAccountBalanceIfAvailable(String username, double adjustment, 
                                                            String comment, String accountName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(username);
        params.add(adjustment);
        params.add(trimToEmpty(comment));
        params.add(trimToEmpty(accountName));
        return ((Boolean) execute("api.adjustUserAccountBalanceIfAvailable", params)).booleanValue();
    }
    
    /**
     * Adjust a user's account balance by an adjustment amount (if there is credit available to leave the specified
     * amount still available in the account).   This can be used to perform atomic account adjustments, without need
     * to check the user's balance first.
     * <p>
     * An adjustment may be positive (add to the user's account) or negative (subtract from the account).
     * 
     * @param username The username associated with the user who's account is to be adjusted.
     * @param adjustment The adjustment amount. Positive to add credit and negative to subtract.
     * @param leaveRemaining The
     * @param comment A user defined comment to be associated with the transaction. This may be a null string.
     * @return Returns true if the adjustment was allowed. Returns false if the user didn't have enough available
     *          credit.
     */
    public boolean adjustUserAccountBalanceIfAvailableLeaveRemaining(String username, double adjustment, 
                                                                    double leaveRemaining, String comment) {
        return adjustUserAccountBalanceIfAvailableLeaveRemaining(username, adjustment, leaveRemaining, comment, "");
    }
    
    /**
     * Adjust a user's account balance by an adjustment amount (if there is credit available to leave the specified
     * amount still available in the account).   This can be used to perform atomic account adjustments, without need
     * to check the user's balance first.
     * <p>
     * An adjustment may be positive (add to the user's account) or negative (subtract from the account).
     * 
     * @param username The username associated with the user who's account is to be adjusted.
     * @param adjustment The adjustment amount. Positive to add credit and negative to subtract.
     * @param leaveRemaining The
     * @param comment A user defined comment to be associated with the transaction. This may be a null string.
     * @param accountName Optional name of the user's personal account. If blank, the built-in default account is used.
     *                      If multiple personal accounts is enabled the account name must be provided.
     * @return Returns true if the adjustment was allowed. Returns false if the user didn't have enough available
     *          credit.
     */
    public boolean adjustUserAccountBalanceIfAvailableLeaveRemaining(String username, double adjustment, 
                                                                    double leaveRemaining, String comment,
                                                                    String accountName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(username);
        params.add(adjustment);
        params.add(leaveRemaining);
        params.add(trimToEmpty(comment));
        params.add(trimToEmpty(accountName));
        return ((Boolean) execute("api.adjustUserAccountBalanceIfAvailableLeaveRemaining", params)).booleanValue();
    }

    /**
     * Adjust the account balance of all users in a group by an adjustment amount. An adjustment may be positive 
     * (add to the user's account) or negative (subtract from the account).
     * 
     * @param group The group for which all users' accounts are to be adjusted.
     * @param adjustment The adjustment amount. Positive to add credit and negative to subtract.
     * @param comment A user defined comment to be associated with the transaction. This may be a null string.
     */
    public void adjustUserAccountBalanceByGroup(String group, double adjustment, String comment) {
        adjustUserAccountBalanceByGroup(group, adjustment, comment, "");
    }

    /**
     * Adjust the account balance of all users in a group by an adjustment amount. An adjustment may be positive 
     * (add to the user's account) or negative (subtract from the account).
     * 
     * @param group The group for which all users' accounts are to be adjusted.
     * @param adjustment The adjustment amount. Positive to add credit and negative to subtract.
     * @param comment A user defined comment to be associated with the transaction. This may be a null string.
     * @param accountName Optional name of the user's personal account. If blank, the built-in default account is used.
     *                      If multiple personal accounts is enabled the account name must be provided.
     */
    public void adjustUserAccountBalanceByGroup(String group, double adjustment, String comment, String accountName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(group);
        params.add(adjustment);
        params.add(trimToEmpty(comment));
        params.add(trimToEmpty(accountName));
        execute("api.adjustUserAccountBalanceByGroup", params);
    }

    /**
     * Adjust the account balance of all users in a group by an adjustment amount. An adjustment may be positive 
     * (add to the user's account) or negative (subtract from the account).  Balance will not be increased beyond the
     * given limit.
     *
     * @param group The group for which all users' accounts are to be adjusted.
     * @param adjustment The adjustment amount. Positive to add credit and negative to subtract.
     * @param limit Only add balance up to this limit.
     * @param comment A user defined comment to be associated with the transaction. This may be a null string.
     */
    public void adjustUserAccountBalanceByGroupUpTo(String group, double adjustment, double limit, String comment) {
        adjustUserAccountBalanceByGroupUpTo(group, adjustment, limit, comment, "");
    }

    /**
     * Adjust the account balance of all users in a group by an adjustment amount. An adjustment may be positive 
     * (add to the user's account) or negative (subtract from the account).  Balance will not be increased beyond the
     * given limit.
     *
     * @param group The group for which all users' accounts are to be adjusted.
     * @param adjustment The adjustment amount. Positive to add credit and negative to subtract.
     * @param limit Only add balance up to this limit.
     * @param comment A user defined comment to be associated with the transaction. This may be a null string.
     * @param accountName Optional name of the user's personal account. If blank, the built-in default account is used.
     *                      If multiple personal accounts is enabled the account name must be provided.
     */
    public void adjustUserAccountBalanceByGroupUpTo(String group, double adjustment, double limit, String comment,
            String accountName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(group);
        params.add(adjustment);
        params.add(limit);
        params.add(trimToEmpty(comment));
        params.add(trimToEmpty(accountName));
        
        execute("api.adjustUserAccountBalanceByGroupUpTo", params);
    }

    /**
     * Set the balance on a user's account to a set value. This is conducted as a transaction.
     * 
     * @param username The username associated with the user who's account is to be set.
     * @param balance The balance to set the account to.
     * @param comment A user defined comment to associate with the transaction. This may be a null string.
     */
    public void setUserAccountBalance(String username, double balance, String comment) {
        setUserAccountBalance(username, balance, comment, "");
    }
    
    /**
     * Set the balance on a user's account to a set value. This is conducted as a transaction.
     * 
     * @param username The username associated with the user who's account is to be set.
     * @param balance The balance to set the account to.
     * @param comment A user defined comment to associate with the transaction. This may be a null string.
     * @param accountName Optional name of the user's personal account. If blank, the built-in default account is used.
     *                      If multiple personal accounts is enabled the account name must be provided.
     */
    public void setUserAccountBalance(String username, double balance, String comment, String accountName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(username);
        params.add(balance);
        params.add(trimToEmpty(comment));
        params.add(trimToEmpty(accountName));
        execute("api.setUserAccountBalance", params);
    }

    /**
     * Set the balance for each member of a group to the given value.
     *
     * @param group The group for which all users' balance is to be set.
     * @param balance The value to set all users' balance to.
     * @param comment A user defined comment to associate with the transaction. This may be a null string.
     */
    public void setUserAccountBalanceByGroup(String group, double balance, String comment) {
        setUserAccountBalanceByGroup(group, balance, comment, "");
    }

    /**
     * Set the balance for each member of a group to the given value.
     *
     * @param group The group for which all users' balance is to be set.
     * @param balance The value to set all users' balance to.
     * @param comment A user defined comment to associate with the transaction. This may be a null string.
     * @param accountName Optional name of the user's personal account. If blank, the built-in default account is used.
     *                      If multiple personal accounts is enabled the account name must be provided.
     */
    public void setUserAccountBalanceByGroup(String group, double balance, String comment, String accountName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(group);
        params.add(balance);
        params.add(trimToEmpty(comment));
        params.add(trimToEmpty(accountName));
        execute("api.setUserAccountBalanceByGroup", params);
    }

    /**
     * Reset the counts (pages and job counts) associated with a user account.
     * 
     * @param username The username associated with the user who's counts are to be reset.
     * @param resetBy The name of the user/script/process reseting the counts.
     */
    public void resetUserCounts(String username, String resetBy) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(username);
        params.add(resetBy);
        execute("api.resetUserCounts", params);
    }
    
    /**
     * Re-applies initial user settings on the given user. These initial settings are based on group membership.
     * @param username the username
     */
    public void reapplyInitialUserSettings(String username) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(username);
        execute("api.reapplyInitialUserSettings", params);
    }

    /**
     * Change a user's account selection setting to automatically charge to a single shared account.
     *
     * @param username The user's username
     * @param accountName The shared account name
     * @param chargeToPersonal true if charge to personal and allocate to shared account. 
     */
    public void setUserAccountSelectionAutoSelectSharedAccount(String username, String accountName, 
            boolean chargeToPersonal) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(username);
        params.add(accountName);
        params.add(chargeToPersonal);
        execute("api.setUserAccountSelectionAutoSelectSharedAccount", params);
    }

    /**
     * Change a user's account selection setting to use the standard account selection pop-up.
     *
     * @param username The user's username
     * @param allowPersonal allow user to charge to personal account
     * @param allowListSelection allow user to select an account from the list of shared account
     * @param allowPinCode allow user to select a shared account using pin ode
     * @param allowPrintingAsOtherUser allow user to charge to other users 
     * @param chargeToPersonalWhenSharedSelected true if charge to personal and allocate to shared account. 
     */
    
    public void setUserAccountSelectionStandardPopup(String username, boolean allowPersonal, 
            boolean allowListSelection, boolean allowPinCode, boolean allowPrintingAsOtherUser, 
            boolean chargeToPersonalWhenSharedSelected) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(username);
        params.add(allowPersonal);
        params.add(allowListSelection);
        params.add(allowPinCode);
        params.add(allowPrintingAsOtherUser);
        params.add(chargeToPersonalWhenSharedSelected);
        execute("api.setUserAccountSelectionStandardPopup", params);
    }
    
    /** Sets the user to auto charge to it's personal account. 
     * 
     * @param username The user's username
     * 
     */
    public void setUserAccountSelectionAutoChargePersonal(String username) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(username);
        execute("api.setUserAccountSelectionAutoChargePersonal", params);
    }
    
    /**
     * Disable printing for a user.
     * 
     * @param userName The name of the server hosting the printer.
     * @param disableMins The number of minutes to disable printing for.
     */
    public void disablePrintingForUser(String userName, int disableMins) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(userName);
        params.add(disableMins);
        execute("api.disablePrintingForUser", params);
    }

    /**
     * Delete a printer.
     *
     * @param serverName The name of the server hosting the printer.
     * @param printerName The name of the printer to be deleted. To delete all printers on the defined server, 
     *                    set to the special text "[All Printers]".
     */
    public void deletePrinter(String serverName, String printerName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(serverName);
        params.add(printerName);
        execute("api.deletePrinter", params);
    }

    /**
     * Rename a printer.  This can be useful after migrating a print queue or print server (i.e. the printer retains its
     * history and settings under the new name).  Note that in some cases case sensitivity is important, so care should
     * be taken to enter the name exactly as it appears in the OS.
     *
     * @param serverName The existing printer's server name.
     * @param printerName The existing printer's queue name.
     * @param newServerName The new printer's server name.
     * @param newPrinterName The new printer's queue name.
     */
    public void renamePrinter(String serverName, String printerName, String newServerName, String newPrinterName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(serverName);
        params.add(printerName);
        params.add(newServerName);
        params.add(newPrinterName);
        execute("api.renamePrinter", params);
    }
    
    /**
     * Adds the group to the printer access group list. 
     * 
     * @param serverName The name of the server name
     * @param printerName The name of the printer. 
     * @param groupName name of the group that needs to be added to the 
     *        list of groups that are allowed to print to this printer.
     */
    public void addPrinterAccessGroup(String serverName, String printerName, String groupName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(serverName);
        params.add(printerName);
        params.add(groupName);
        execute("api.addPrinterAccessGroup", params);
    }
    
    /**
     * Removes the group from the printer access group list. 
     * 
     * @param serverName The name of the server name
     * @param printerName The name of the printer. 
     * @param groupName group name that needs to be removed from the list of groups allowed to print to this printer.
     */
    public void removePrinterAccessGroup(String serverName, String printerName, String groupName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(serverName);
        params.add(printerName);
        params.add(groupName);
        execute("api.removePrinterAccessGroup", params);
    }

    /**
     * Trigger the process of adding a new user account. Assuming the user exists in the OS/Network/Domain user
     * directory, the account will be created with the correct initial settings as defined by the rules setup in the
     * admin interface under the Group's section.
     * 
     * Calling this method is equivalent to triggering the "new user" event when a new user performs printing for the
     * first time.
     * 
     * @param username The username of the user to add.
     */
    public void addNewUser(String username) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(username);
        execute("api.addNewUser", params);
    }

    /**
     * Rename a user account.  Useful when the user has been renamed in the domain / directory, so that usage history
     * can be maintained for the new username.  This should be performed in conjunction with a rename of the user in the
     * domain / user directory, as all future usage and authentication will need to use the new username.
     *
     * @param currentUserName The username of the user to rename.
     * @param newUserName The user's new username.
     */
    public void renameUserAccount(String currentUserName, String newUserName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(currentUserName);
        params.add(newUserName);
        execute("api.renameUserAccount", params);
    }

    /**
     * Delete/remove an existing user from the user list. Use this method with care.  Calling this will
     * perminantly delete the user account from the user list (print & transaction history records remain).
     *
     * @param username The username of the user to delete.
     */
    public void deleteExistingUser(String username) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(username);
        execute("api.deleteExistingUser", params);
    }

    /**
     * Creates and sets up a new internal user account.  The (unique) username and password are required at a minimum.
     * The other properties are optional and will be used if not blank.  Properties may also be set after creation using
     * {@link #setUserProperty} or {@link #setUserProperties}.
     *
     * @param username (required) A unique username.  An exception is thrown if the username already exists.
     * @param password (required) The user's password.
     * @param fullName (optional) The full name of the user.
     * @param email (optional) The email address of the user.
     * @param cardId (optional) The card/identity number of the user.
     * @param pin The card/id pin.
     */
    public void addNewInternalUser(String username, String password, String fullName, String email, String cardId,
            String pin) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(username);
        params.add(password);
        params.add(fullName);
        params.add(email);
        params.add(cardId);
        params.add(pin);
        execute("api.addNewInternalUser", params);
    }

    /**
     * Looks up the user with the given user id number and returns their user name.  If no match was found an empty
     * string is returned.
     *
     * @param idNo The user id number to look up.
     * @return The matching user name, or an empty string if there was no match.
     */
    public String lookUpUserNameByIDNo(String idNo) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(idNo);
        return (String) execute("api.lookUpUserNameByIDNo", params);
    }

    /**
     * Looks up the user with the given user card number and returns their user name.  If no match was found an empty
     * string is returned.
     *
     * @param cardNo The user card number to look up.
     * @return The matching user name, or an empty string if there was no match.
     */
    public String lookUpUserNameByCardNo(String cardNo) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(cardNo);
        return (String) execute("api.lookUpUserNameByCardNo", params);
    }

    /**
     * Adds the user to the specified group. 
     * @param userName The name of the user
     * @param groupName The name of the group.
     */
    public void addUserToGroup(String userName, String groupName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(userName);
        params.add(groupName);
        execute("api.addUserToGroup", params);
    }

    /**
     * Removes the user from the specified group. 
     * @param userName The name of the user
     * @param groupName The name of the group.
     */
    public void removeUserFromGroup(String userName, String groupName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(userName);
        params.add(groupName);
        execute("api.removeUserFromGroup", params);
    }

    /**
     * Adds a user as administrator with the default admin rights. 
     * @param userName The name of the user
     */
    public void addAdminAccessUser(String userName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(userName);
        execute("api.addAdminAccessUser", params);
    }
    
    /**
     * Removes an admin user from the list of admins.
     * @param userName The name of the user
     */
    public void removeAdminAccessUser(String userName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(userName);
        execute("api.removeAdminAccessUser", params);
    }
    
    /**
     * Adds a group as an admin group with the default admin rights. 
     * @param groupName The name of the group
     */
    public void addAdminAccessGroup(String groupName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(groupName);
        execute("api.addAdminAccessGroup", params);
    }
    
    /**
     * Removes a group from the list of admin groups.
     * @param groupName The name of the group
     */
    public void removeAdminAccessGroup(String groupName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(groupName);
        execute("api.removeAdminAccessGroup", params);
    }
    
    /**
     * List all user accounts (sorted by username) starting at <code>offset</code> and ending at <code>limit</code>.
     * This can be used to enumerate all user accounts in 'pages'.  When retrieving a list of all user accounts, the
     * recommended page size / limit is <code>1000</code>.  Batching in groups of 1000 ensures efficient transfer and
     * processing.
     * E.g.:
     *   listUserAccounts(0, 1000) - returns users 0 through 999
     *   listUserAccounts(1000, 1000) - returns users 1000 through 1999
     *   listUserAccounts(2000, 1000) - returns users 2000 through 2999
     *
     * @param offset The 0-index offset in the list of users to return.  I.e. 0 is the first user, 1 is the second, etc.
     * @param limit The number of users to return in this batch.  Recommended: <code>1000</code>.
     * @return A <code>Vector</code> of user names.
     */
    @SuppressWarnings("unchecked")
    public Vector<String> listUserAccounts(int offset, int limit) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(offset);
        params.add(limit);
        return (Vector<String>) execute("api.listUserAccounts", params);
    }

    /**
     * List all shared accounts (sorted by account name) starting at <code>offset</code> and ending at <code>limit</code>.
     * This can be used to enumerate all shared accounts in 'pages'.  When retrieving a list of all shared accounts, the
     * recommended page size / limit is <code>1000</code>.  Batching in groups of 1000 ensures efficient transfer and
     * processing.
     * E.g.:
     *   listSharedAccounts(0, 1000) - returns accounts 0 through 999
     *   listSharedAccounts(1000, 1000) - returns accounts 1000 through 1999
     *   listSharedAccounts(2000, 1000) - returns accounts 2000 through 2999
     *
     * @param offset The 0-index offset in the list of accounts to return.  I.e. 0 is the first account, 1 is the second, etc.
     * @param limit The number of account to return in this batch.  Recommended: <code>1000</code>.
     * @return A <code>Vector</code> of shared account names.
     */
    @SuppressWarnings("unchecked")
    public Vector<String> listSharedAccounts(int offset, int limit) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(offset);
        params.add(limit);
        return (Vector<String>) execute("api.listSharedAccounts", params);
    }
    
    /**
     * Get the count of all users in the system.
     *
     * @return Numeric total of all user accounts.
     */
    @SuppressWarnings("unchecked")
    public int getTotalUsers() {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        return (Integer) execute("api.getTotalUsers", params);
    }
    
    /**
     * List all shared accounts (sorted by account name) that the user has access to, starting at <code>offset</code> 
     * and listing only <code>limit</code> accounts. This can be used to enumerate all shared accounts in 'pages'.  
     * When retrieving a list of all shared accounts, the recommended page size / limit is <code>1000</code>.  
     * Batching in groups of 1000 ensures efficient transfer and processing.
     * E.g.:
     *   listUserSharedAccounts("user", 0, 1000) - returns accounts 0 through 999
     *   listUserSharedAccounts("user", 1000, 1000) - returns accounts 1000 through 1999
     *   listUserSharedAccounts("user", 2000, 1000) - returns accounts 2000 through 2999
     *
     * @param userName The username of the user to get the accounts for.
     * @param offset The 0-index offset in the list of accounts to return.  I.e. 0 is the first account, 1 is the second, etc.
     * @param limit The number of account to return in this batch.  Recommended: <code>1000</code>.
     * @return A <code>Vector</code> of shared account names the user has access to.
     */
    @SuppressWarnings("unchecked")
    public Vector<String> listUserSharedAccounts(String userName, int offset, int limit) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(userName);
        params.add(offset);
        params.add(limit);
        return (Vector<String>) execute("api.listUserSharedAccounts", params);
    }

    /**
     * Test to see if a shared account exists.
     * 
     * @param accountName The name of the shared account.
     * @return Return true if the shared account exists, else false.
     */
    public boolean isSharedAccountExists(String accountName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(accountName);
        Boolean r = (Boolean) execute("api.isSharedAccountExists", params);
        return r.booleanValue();
    }
    
    /**
     * The current account balance associated with a shared account.
     * 
     * @param accountName
     *            The account's full name.
     * @return The shared account's account balance as a double.
     */
    public double getSharedAccountAccountBalance(String accountName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(accountName);
        Double r = (Double) execute("api.getSharedAccountAccountBalance", params);
        return r.doubleValue();
    }

    /**
     * Gets a shared account property.
     *
     * @param sharedAccountName The name of the shared account.
     * @param propertyName The name of the property to get.  Valid options include: access-groups, access-users,
     * balance, comment-option, disabled, invoice-option, notes, pin, restricted.
     * @return The value of the requested property.
     *
     * @see #setSharedAccountProperty
     */
    public String getSharedAccountProperty(String sharedAccountName, String propertyName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(sharedAccountName);
        params.add(propertyName);
        return (String) execute("api.getSharedAccountProperty", params);
    }

    /**
     * Get multiple shared account properties at once (to save multiple calls).
     *
     * @param sharedAccountName The shared account name.
     * @param propertyNames The names of the properties to get.  See
     * {@link #getSharedAccountProperty #getSharedAccountProperty} for valid property names.
     * @return The property values (in the same order as given in {@code propertyNames}.
     *
     * @see #getSharedAccountProperty
     * @see #setSharedAccountProperties
     */
    @SuppressWarnings("unchecked")
    public Vector<String> getSharedAccountProperties(String sharedAccountName, Vector<String> propertyNames) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(sharedAccountName);
        params.add(propertyNames);
        return (Vector<String>) execute("api.getSharedAccountProperties", params);
    }

    /**
     * Sets a shared account property.
     *
     * @param sharedAccountName The name of the shared account.
     * @param propertyName The name of the property to get.  See
     * {@link #getSharedAccountProperty #getSharedAccountProperty} for valid property names.
     * @param propertyValue The value of the property to set.
     *
     * @see #getSharedAccountProperty
     */
    public void setSharedAccountProperty(String sharedAccountName, String propertyName, String propertyValue) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(sharedAccountName);
        params.add(propertyName);
        params.add(propertyValue);
        execute("api.setSharedAccountProperty", params);
    }

    /**
     * Set multiple shared account properties at once (to save multiple calls).
     *
     * @param sharedAccountName The shared account name.
     * @param propertyNamesAndValues The list of property names and values to set.
     * E.g. [["balance", "1.20"], ["invoice-option", "ALWAYS_INVOICE"]].  See
     * {@link #setSharedAccountProperty #setSharedAccountProperty} for valid property names.
     *
     * @see #getSharedAccountProperties
     * @see #setSharedAccountProperty
     */
    public void setSharedAccountProperties(String sharedAccountName, Vector<Vector<String>> propertyNamesAndValues) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(sharedAccountName);
        params.add(propertyNamesAndValues);
        execute("api.setSharedAccountProperties", params);
    }

    /**
     * Adjust a shared account's account balance by an adjustment amount. An adjustment bay be positive 
     * (add to the account) or negative (subtract from the account).
     * 
     * @param accountName The full name of the shared account to adjust.
     * @param adjustment The adjustment amount. Positive to add credit and negative to subtract.
     * @param comment A user defined comment to associated with the transaction. This may be a null string.
     */
    public void adjustSharedAccountAccountBalance(String accountName, double adjustment, String comment) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(accountName);
        params.add(adjustment);
        params.add(trimToEmpty(comment));
        execute("api.adjustSharedAccountAccountBalance", params);
    }

    /**
     * Set the balance on a shared account to a set value. This is conducted as a transaction.
     *
     * @param accountName The full account name of the account to be set.
     * @param balance The balance to set the account to.
     * @param comment A user defined comment to associate with the transaction. This may be a null string.
     */
    public void setSharedAccountAccountBalance(String accountName, double balance, String comment) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(accountName);
        params.add(balance);
        params.add(trimToEmpty(comment));
        execute("api.setSharedAccountAccountBalance", params);
    }

    /**
     * Create a new shared account with the given name.
     *
     * @param sharedAccountName The name of the shared account to create. Use a '\' to denote a subaccount,
     *                          e.g.: 'parent\sub'
     */
    public void addNewSharedAccount(String sharedAccountName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(sharedAccountName);
        execute("api.addNewSharedAccount", params);
    }

    /**
     * Rename an existing shared account. 
     * 
     * @param currentSharedAccountName The name of the shared account to rename. Use a '\' to denote a subaccount,
     *                                 e.g.: 'parent\sub'
     * @param newSharedAccountName The new shared account name
     */
    public void renameSharedAccount(String currentSharedAccountName, String newSharedAccountName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(currentSharedAccountName);
        params.add(newSharedAccountName);
        execute("api.renameSharedAccount", params);
    }
    
    /**
     * Delete a shared account from the system.  Use this method with care.  Deleting a shared account will permanently
     * delete it from the shared account list (print history records will remain).
     *
     * @param sharedAccountName The name of the shared account to delete.
     */
    public void deleteExistingSharedAccount(String sharedAccountName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(sharedAccountName);
        execute("api.deleteExistingSharedAccount", params);
    }

    /**
     * Allow the given user access to the given shared account without using a pin.
     *
     * @param sharedAccountName The name of the shared account to allow access to.
     * @param userName The name of the user to give access to.
     */
    public void addSharedAccountAccessUser(String sharedAccountName, String userName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(sharedAccountName);
        params.add(userName);
        execute("api.addSharedAccountAccessUser", params);
    }

    /**
     * Allow the given group access to the given shared account without using a pin.
     *
     * @param sharedAccountName The name of the shared account to allow access to.
     * @param groupName The name of the group to give access to.
     */
    public void addSharedAccountAccessGroup(String sharedAccountName, String groupName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(sharedAccountName);
        params.add(groupName);
        execute("api.addSharedAccountAccessGroup", params);
    }

    /**
     * Revoke the given user's access to the given shared account.
     *
     * @param sharedAccountName The name of the shared account to revoke access to.
     * @param userName The name of the user to revoke access for.
     */
    public void removeSharedAccountAccessUser(String sharedAccountName, String userName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(sharedAccountName);
        params.add(userName);
        execute("api.removeSharedAccountAccessUser", params);
    }

    /**
     * Revoke the given group's access to the given shared account.
     *
     * @param sharedAccountName The name of the shared account to revoke access to.
     * @param groupName The name of the group to revoke access for.
     */
    public void removeSharedAccountAccessGroup(String sharedAccountName, String groupName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(sharedAccountName);
        params.add(groupName);
        execute("api.removeSharedAccountAccessGroup", params);
    }




    /**
     * Gets a printer property.
     *
     * @param serverName The name of the server.
     * @param printerName The name of the printer.
     * @param propertyName The name of the property.  Valid options include: disabled, 
     *                      print-stats.job-count, print-stats.page-count, cost-model
     * @return The value of the requested property.
     */
    public String getPrinterProperty(String serverName, String printerName, String propertyName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(serverName);
        params.add(printerName);
        params.add(propertyName);
        return (String) execute("api.getPrinterProperty", params);
    }

    /**
     * Sets a printer property.
     *
     * @param serverName The name of the server.
     * @param printerName The name of the printer.
     * @param propertyName The name of the property.  Valid options include: disabled.
     * @param propertyValue The value of the property to set.
     */
    public void setPrinterProperty(String serverName, String printerName, String propertyName, String propertyValue) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(serverName);
        params.add(printerName);
        params.add(propertyName);
        params.add(propertyValue);
        execute("api.setPrinterProperty", params);
    }
    
    /**
     * List all printers sorted by printer name. This can be used to enumerate all printers in 'pages'.  
     * When retrieving a list of all printers, the recommended page size / limit is <code>1000</code>.  
     * Batching in groups of 1000 ensures efficient transfer and
     * processing.
     * E.g.:
     *   listPrinters(0, 1000) - returns printers 0 through 999
     *   listPrinters(1000, 1000) - returns printers 1000 through 1999
     *   listPrinters(2000, 1000) - returns printers 2000 through 2999
     *
     * @param offset The 0-index offset in the list of printers to return.  I.e. 0 is the first printer, 
     *               1 is the second, etc.
     * @param limit The number of printers to return in this batch.  Recommended: <code>1000</code>.
     * @return A <code>Vector</code> of printer names.
     */
    @SuppressWarnings("unchecked")
    public Vector<String> listPrinters(int offset, int limit) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(offset);
        params.add(limit);
        return (Vector<String>) execute("api.listPrinters", params);
    }

    /**
     * Reset the counts (pages and job counts) associated with a printer.
     * 
     * @param serverName The name of the server hosting the printer.
     * @param printerName The printer's name.
     * @param resetBy The name of the user/script/process resetting the counts.
     */
    public void resetPrinterCounts(String serverName, String printerName, String resetBy) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(serverName);
        params.add(printerName);
        params.add(resetBy);
        execute("api.resetPrinterCounts", params);
    }

    /**
     * Add a printer to a single printer group.
     * @param serverName The name of the server hosting the printer.
     * @param printerName The printer's name.
     * @param printerGroupName Name of a printer group. 
     */
    public void addPrinterGroup(String serverName, String printerName, String printerGroupName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(serverName);
        params.add(printerName);
        params.add(printerGroupName);
        execute("api.addPrinterGroup", params);
    }
    
    /**
     * Set the printer groups a printer belongs to, overwriting any existing group.
     * @param serverName The name of the server hosting the printer.
     * @param printerName The printer's name.
     * @param printerGroupNames A comma separated list of printer group names. 
     *                          To clear all group association set to "".
     */
    public void setPrinterGroups(String serverName, String printerName, String printerGroupNames) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(serverName);
        params.add(printerName);
        params.add(printerGroupNames);
        execute("api.setPrinterGroups", params);
    }

    /**
     * Enable a printer.
     * 
     * @param serverName The name of the server hosting the printer.
     * @param printerName The printer's name.
     */
    public void enablePrinter(String serverName, String printerName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(serverName);
        params.add(printerName);
        execute("api.enablePrinter", params);
    }

    /**
     * Disable a printer for select period of time.
     * 
     * @param serverName The name of the server hosting the printer.
     * @param printerName The printer's name.
     * @param disableMins The number of minutes to disable the printer. If the value is -1 the printer will be disabled for all
     *                    time until re-enabled.
     */
    public void disablePrinter(String serverName, String printerName, int disableMins) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(serverName);
        params.add(printerName);
        params.add(disableMins);
        execute("api.disablePrinter", params);
    }

    /**
     * Method to set a simple single page cost using the Simple Charging Model.
     * 
     * @param serverName The name of the server.
     * @param printerName The name of the printer.
     * @param costPerPage The cost per page (simple charging model)
     */
    public void setPrinterCostSimple(String serverName, String printerName, double costPerPage) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(serverName);
        params.add(printerName);
        params.add(costPerPage);
        execute("api.setPrinterCostSimple", params);
    }
    
    /**
     * Get the page cost if, and only if, the printer is using the Simple Charging Model.
     * @param serverName The name of the server.
     * @param printerName The name of the printer.
     * @return The default page cost. On failure an exception is thrown.
     */
    public double getPrinterCostSimple(String serverName, String printerName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(serverName);
        params.add(printerName);
        return ((Double) execute("api.getPrinterCostSimple", params)).doubleValue();
    }
    
    /**
     * Add a new group to system's group list.  The caller is responsible for ensuring that the supplied group name
     * is valid and exists in the linked user directory source.  The status of this method may be monitored with 
     * calls to <code>getTaskStatus()</code> .
     * 
     * @param groupName The name of the new group to add. The group should already exist in the network user directory.
     */
    public void addNewGroup(String groupName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(groupName);
        execute("api.addNewGroup", params);
    }
    
    /**
     *Syncs an existing group with the configured directory server, updates the group membership.
     * @param groupName The name of the group to sync
     * @return TRUE if successful.  On failure an exception is thrown.
     */
    public boolean syncGroup(String groupName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(groupName);
        Boolean result = (Boolean) execute("api.syncGroup", params);
        return result.booleanValue();  
    }

    /**
     * Removes an  already existing group. 
     * @param groupName The name of the group that needs to be removed. The group should already exist in PaperCut.
     */
    public void removeGroup(String groupName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(groupName);
        execute("api.removeGroup", params);
    }
    
    /**
     * Retrieves a list of a single user's group memberships.
     * @param userName The name of the user whose group memberships are to be retrieved.
     * @return a <code>Vector</code> of group names.
     */
    @SuppressWarnings("unchecked")
    public Vector<String> getUserGroups(String userName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(userName);
        return (Vector<String>) execute("api.getUserGroups", params);
    }
    
    
    
    /**
    * List user groups. This can be used to enumerate all user groups in 'pages'.  When retrieving a list of
    * all user groups, the recommended page size / limit is <code>1000</code>.  Batching in groups of 
    * 1000 ensures efficient transfer and
    * processing.
    * E.g.:
    *   listUserGroups(0, 1000) - returns groups 0 through 999
    *   listUserGroups(1000, 1000) - returns groups 1000 through 1999
    *   listUserGroups(2000, 1000) - returns groups 2000 through 2999
    *
    * @param offset The 0-index offset in the list of groups to return.  I.e. 0 is the first group, 
    *               1 is the second, etc.
    * @param limit The number of groups to return in this batch.  Recommended: <code>1000</code>.
    * @return A <code>Vector</code> of group names.
    */
    @SuppressWarnings("unchecked")
    public Vector<String> listUserGroups(int offset, int limit) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(offset);
        params.add(limit);
        return (Vector<String>) execute("api.listUserGroups", params);
    }

    /**
     * Checks if group exists or not.
     *
     * @param groupName The group name to check.
     * @return {@code true} if the group exists, {@code false} if it doesn't.
     */
    public boolean isGroupExists(String groupName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(groupName);
        Boolean result = (Boolean) execute("api.isGroupExists", params);
        return result.booleanValue();
    }

    /**
     * Set the group quota allocation settings on a given group.
     * 
     * @param groupName The name of the group.
     * @param quotaAmount The quota amount to set.
     * @param period The schedule period (one of either NONE, DAILY, WEEKLY or MONTHLY);
     * @param quotaMaxAccumulation The maximum quota accumulation.  Set to 0.0 to have no limit.
     */
    public void setGroupQuota(String groupName, double quotaAmount, String period, double quotaMaxAccumulation) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(groupName);
        params.add(quotaAmount);
        params.add(period);
        params.add(quotaMaxAccumulation);
        execute("api.setGroupQuota", params);
    }
    
    /**
     * Get the group quota allocation settings on a given group.
     * 
     * @param groupName The name of the group.
     * @return A Hashtable (XML-RPC Struct) containing the information in keys:
     *              QuotaAmount, QuotaPeriod, QuotaMaxAccumulation.
     */
    @SuppressWarnings("unchecked")
    public Hashtable<String, Object> getGroupQuota(String groupName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(groupName);
        return (Hashtable<String, Object>) execute("api.getGroupQuota", params);
    }

    /**
     * Add the value of the a card to a user's account.
     * @param cardNumber The number of the card to use.
     * @param userName The username with the account to credit.
     * @return A string indicating the outcome, such as SUCCESS, UNKNOWN_USER, INVALID_CARD_NUMBER, CARD_IS_USED or
     *         CARD_HAS_EXPIRED.
     */
    public String useCard(String userName, String cardNumber) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(userName);
        params.add(cardNumber);
        return (String) execute("api.useCard", params);
    }

    /**
     * Return the status (completed flag and any status message text) associated with a long running task such as
     * a sync operation started by the performGroupSync API.
     * 
     * @return A <code>TaskStatus</code> object providing information about the current or latest task started via
     * this API.
     */
    @SuppressWarnings("unchecked")
    public TaskStatus getTaskStatus() {
        Vector<Object> params = new Vector<Object>();
        Hashtable<String, Object> ret = (Hashtable<String, Object>) execute("api.getTaskStatus", params);
        TaskStatus ts = new TaskStatus();
        ts.setCompleted((Boolean) ret.get("completed"));
        ts.setMessage((String) ret.get("message"));
        return ts;
    }

    /**
     * Instigate an online backup. This process is equivalent to pressing the manual backup button in the web based
     * admin interface. The data is expected into the server/data/backups directory as a timestamped, zipped XML file.
     */
    public void performOnlineBackup() {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        execute("api.performOnlineBackup", params);
    }

    /**
     * Start the process of synchronizing the system's group membership with the OS/Network/Domain's group membership.
     * The call to this method will start the synchronization process. The operation will commence and complete in the
     * background.
     */
    public void performGroupSync() {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        execute("api.performGroupSync", params);
    }

    /**
     * Start a full user and group synchronization. This is equivalent to pressing on the "Synchronize Now" button in
     * the admin user interface. The behaviour of the sync process, such as deleting old users, is determined by the
     * current system settings as defined in the admin interface. A call to this method will commence the sync process
     * and the operation will complete in the background.
     */
    public void performUserAndGroupSync() {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        execute("api.performUserAndGroupSync", params);
    }

    /**
     * An advanced version of the user and group synchronization process providing control over the sync behaviour. A
     * call to this method will commence the sync process and the operation will complete in the background.
     * 
     * @param deleteNonExistentUsers If set to <CODE>True</CODE>, old users will be deleted.
     * @param updateUserDetails If set to <CODE>True</CODE>, user details such as full-name, email, etc. will be synced
     *                          with the underlying OS/Network/Domain user directory.
     */
    public void performUserAndGroupSyncAdvanced(boolean deleteNonExistentUsers, boolean updateUserDetails) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(deleteNonExistentUsers);
        params.add(updateUserDetails);
        execute("api.performUserAndGroupSyncAdvanced", params);
    }

    /**
     * Calling this method will start a specialized user and group synchronization process optimized for tracking down
     * adding any new users that exist in the OS/Network/Domain user directory and not in the system. Any existing user
     * accounts will not be modified. A group synchronization will only be performed if new users are actually added to
     * the system.
     */
    public void addNewUsers() {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        execute("api.addNewUsers", params);
    }

    /**
     * Import the shared accounts contained in the given tab-delimited import file.
     * @param importFile The import file location relative to the application server.
     * @param test If true, perform a test only. The printed statistics will show what would have occurred if testing
     *             wasn't enabled. No accounts will be modified.
     * @param deleteNonExistentAccounts If true, accounts that do not exist in the import file but exist in the system
     *                                  will be deleted.  If false, they will be ignored.
     * @return Feedback regarding the sync operation.
     */
    public String batchImportSharedAccounts(String importFile, boolean test, boolean deleteNonExistentAccounts) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(importFile);
        params.add(test);
        params.add(deleteNonExistentAccounts);
        return (String) execute("api.batchImportSharedAccounts", params);
    }

    /**
     * Import the internal users contained in the given tab-delimited import file.
     *
     * @param importFile The import file location relative to the application server.
     * @param overwriteExistingPasswords True to overwrite existing user passwords, false to only update un-set
     * passwords.
     * @param overwriteExistingPINs True to overwrite existing user PINs, false to only update un-set PINs.
     */
    public void batchImportInternalUsers(String importFile, boolean overwriteExistingPasswords,
                                         boolean overwriteExistingPINs) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(importFile);
        params.add(overwriteExistingPasswords);
        params.add(overwriteExistingPINs);
        execute("api.batchImportInternalUsers", params);
    }

    /**
     * Import the user card/ID numbers and PINs contained in the given tab-delimited import file.
     * @param importFile The import file location relative to the application server.
     * @param overwriteExistingPINs If true, users with a PIN already defined will have it overwritten by the PIN in the
     *                              import file, if specified.  If false, the existing PIN will not be overwritten.
     */
    public void batchImportUserCardIdNumbers(String importFile, boolean overwriteExistingPINs) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(importFile);
        params.add(overwriteExistingPINs);
        execute("api.batchImportUserCardIdNumbers", params);
    }

    /**
     * Import the user details contained in the given tab-delimited import file.
     * 
     * @param importFile The import file location relative to the application server.
     * @param createNewUsers True to create users if they don't exist; false to just update existing details.
     */
    public void batchImportUsers(String importFile, boolean createNewUsers) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(importFile);
        params.add(createNewUsers);
        execute("api.batchImportUsers", params);
    }

    /**
     * Get the config value from the server.
     * 
     * @param configName The name of the config value to retrieve.
     * @return The config value.  If the config value does not exist a blank string is returned.
     */
    public String getConfigValue(String configName) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(configName);
        return (String) execute("api.getConfigValue", params);
    }

    /**
     * Set the config value from the server.
     * 
     * NOTE: Take care updating config values.  You may cause serious problems which can only be fixed by 
     * reinstallation of the application. Use the setConfigValue API at your own risk.
     * 
     * @param configName The name of the config value to retrieve.
     * @param configValue The value to set.
     */
    public void setConfigValue(String configName, String configValue) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(configName);
        params.add(configValue);
        execute("api.setConfigValue", params);
    }

    /**
     * Takes the details of a job and logs and charges as if it were a "real" job.  Jobs processed via this method are
     * not susceptible to filters, pop-ups, hold/release queues etc., they are simply logged.  See the user manual
     * section "Importing Job Details" for more information and the format of {@code jobDetails}.
     *
     * @param jobDetails The job details (a comma separated list of name-value pairs with an equals sign as the
     * name-value delimiter).
     */
    public void processJob(String jobDetails) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(jobDetails);
        execute("api.processJob", params);
    }

    /**
     * Change the internal admin password.
     * 
     * @param newPassword The new password.  Cannot be blank.
     * @return True if the password was successfully changed.
     */
    public boolean changeInternalAdminPassword(String newPassword) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(newPassword);
        return (Boolean) execute("api.changeInternalAdminPassword", params);
    }
    
    /**
     * Runs a custom command on the server.
     * @param commandName The command name to execute.
     * @param args The arguments to the command.
     * @return The status message returned by the command. 
     */
    public String runCommand(String commandName, Vector<String> args) {
        Vector<Object> params = new Vector<Object>();
        params.add(_authToken);
        params.add(commandName);
        params.add(args);
        return (String) execute("api.runCommand", params);
    }

    /**
     * Executes the XMLRPC method on the server.
     * 
     * @param method
     *            The method to execute.
     * @param parameters
     *            The parameters to the method.
     * @return The value returned from the method.
     */
    private Object execute(String method, Vector<Object> parameters) {
        Object result = null;
        try {
            result = _xmlRpcClient.execute(method, parameters);
            // For some reason XMLRPC 2.0 now returns the XMLRPC exception instead of throwing it
            // This hack detects this and rethrows the exception.
            if (result instanceof XmlRpcException) {
                throw (XmlRpcException) result;
            }
        } catch (XmlRpcException xre) {
            String msg = xre.getMessage();
            // Format message so it's a little cleaner.  Remove any class definitions.
            msg = msg.replaceAll("\\w+(?:\\.+\\w+)+:\\s?", "");
            throw new ServerCommandException("Unable to execute server command. " + msg, xre);
        } catch (Exception e) {
            throw new ServerCommandException("Unable to execute server command. " + e.getMessage(), e);
        }
        return result;
    }
    
    /**
     * Determines if a string is not blank or null.
     * @param str The string to check.
     * @return True if the string is blank or null.
     */
    @SuppressWarnings("unused")
    private boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    /**
     * Determines if a string is blank or null.
     * @param str The string to check.
     * @return True if the string is blank or null.
     */
    private boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param str The string (can be null).
     * @return The trimmed string.  If null then "" is returned.
     */
    private String trimToEmpty(String str) {
        if (str == null) {
            return "";
        }
        return str.trim();
    }

    /**
     * Raised when a server command RCP call fails. A RuntimeException is used to keep the above 
     * method signatures clean.
     * 
     * @author chris
     */
    public static class ServerCommandException extends RuntimeException {
        /**
         * @param message
         *            The reason for the exception.
         */
        public ServerCommandException(String message) {
            super(message);
        }

        /**
         * @param message
         *            The reason for the exception.
         * @param cause
         *            The original cause of the error.
         */
        public ServerCommandException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * A data transfer object used to return task status information.
     * 
     * @author chris
     */
    public static class TaskStatus {
        private boolean _completed;
        private String _message;
        /**
         * @return the completed
         */
        public boolean isCompleted() {
            return _completed;
        }
        /**
         * @param completed the completed to set
         */
        public void setCompleted(boolean completed) {
            _completed = completed;
        }
        /**
         * @return the message
         */
        public String getMessage() {
            return _message;
        }
        /**
         * @param message the message to set
         */
        public void setMessage(String message) {
            _message = message;
        }
        
    }
}
