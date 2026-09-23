package org.esupportail.sgc.services;

import org.springframework.stereotype.Service;
import ua_parser.Client;
import ua_parser.Parser;

/**
 * Remplace l'ancienne librairie eu.bitwalker:UserAgentUtils (abandonnée depuis 2016)
 * par com.github.ua-parser:uap-java.
 *
 * Navigateur et OS ne sont plus renvoyés qu'à granularité "famille" (ex : "Chrome",
 * "Windows", "iOS"), sans numéro de version ni distinction d'appareil : les versions de
 * navigateurs évoluent trop vite pour rester statistiquement utiles, et depuis la "UA string
 * freeze" imposée par les navigateurs (Chrome/Edge/Firefox), l'entête User-Agent ne permet
 * plus de distinguer par exemple Windows 10 de Windows 11 (les deux se présentent comme
 * "Windows NT 10.0"). Voir aussi la migration des données déjà en base dans DbToolService,
 * passage en 3.6.x.
 */
@Service
public class UserAgentParserService {

	public record ParsedUserAgent(String browserName, String osName, String deviceType) {
	}

	private final Parser uaParser = new Parser();

	public ParsedUserAgent parse(String userAgentString) {
		if (userAgentString == null) {
			userAgentString = "";
		}
		Client client = uaParser.parse(userAgentString);
		String browserName = buildBrowserName(client);
		String osName = buildOsName(client);
		String deviceType = buildDeviceType(userAgentString, client);
		return new ParsedUserAgent(browserName, osName, deviceType);
	}

	private String buildBrowserName(Client client) {
		String family = client.userAgent.family;
		if (family == null || family.isEmpty() || "Other".equals(family)) {
			return "";
		}
		if ("Edge".equals(family)) {
			// conserve le libellé historique produit par UserAgentUtils
			family = "Microsoft Edge";
		}
		// Le numéro de version majeur n'a plus d'intérêt statistique (mises à jour trop
		// fréquentes des navigateurs) : on ne conserve que la famille (ex : "Chrome",
		// "Firefox"), cf. migration 3.6.x dans DbToolService qui aligne les données déjà en base.
		return family;
	}

	private String buildOsName(Client client) {
		String family = client.os.family;
		if (family == null || family.isEmpty() || "Other".equals(family)) {
			return "";
		}
		// On ne garde que la famille de l'OS, sans version ni distinction d'appareil
		// (ex : "Windows" pour toutes versions - indiscernables via l'User-Agent depuis la
		// UA string freeze -, "iOS" pour iPhone/iPad, "Mac OS X", "Android", "Chrome OS",
		// "Ubuntu", "Linux"...) : cf. migration 3.6.x dans DbToolService qui aligne les
		// données déjà en base.
		return family;
	}

	private String buildDeviceType(String userAgentString, Client client) {
		String uaLower = userAgentString.toLowerCase();
		String deviceFamily = client.device.family;
		String osFamily = client.os.family;
		boolean isIPad = uaLower.contains("ipad") || (deviceFamily != null && deviceFamily.contains("iPad"));
		if (isIPad) {
			return "Tablet";
		}
		if ("Android".equals(osFamily) && !uaLower.contains("mobile")) {
			// les tablettes Android n'incluent pas le token "Mobile" dans leur User-Agent
			return "Tablet";
		}
		if (uaLower.contains("iphone") || uaLower.contains("mobile") || "Android".equals(osFamily) || "iOS".equals(osFamily)) {
			return "Mobile";
		}
		return "Computer";
	}

}
