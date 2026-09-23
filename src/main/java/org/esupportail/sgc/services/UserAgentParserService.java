package org.esupportail.sgc.services;

import org.springframework.stereotype.Service;
import ua_parser.Client;
import ua_parser.Parser;

/**
 * Remplace l'ancienne librairie eu.bitwalker:UserAgentUtils (abandonnée depuis 2016)
 * par com.github.ua-parser:uap-java, activement maintenue.
 *
 * Depuis la "UA string freeze" imposée par les navigateurs (Chrome/Edge/Firefox), l'entête
 * User-Agent ne permet plus de distinguer Windows 10 de Windows 11 : les deux se présentent
 * comme "Windows NT 10.0". On regroupe donc volontairement ces 2 OS sous le libellé
 * "Windows 10/11" (voir aussi la migration de données dans DbToolService, passage en 3.6.x).
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
		if (client.userAgent.major != null) {
			return family + " " + client.userAgent.major;
		}
		return family;
	}

	private String buildOsName(Client client) {
		String family = client.os.family;
		if (family == null || family.isEmpty() || "Other".equals(family)) {
			return "";
		}
		String deviceFamily = client.device.family;
		if ("iOS".equals(family)) {
			// UserAgentUtils exposait l'OS des idevices comme des variantes de "Mac OS X"
			if (deviceFamily != null && deviceFamily.contains("iPad")) {
				return "Mac OS X (iPad)";
			}
			return "Mac OS X (iPhone)";
		}
		if ("Windows".equals(family)) {
			// Windows 10 et 11 sont indiscernables via le User-Agent (UA string freeze)
			if ("10".equals(client.os.major)) {
				return "Windows 10/11";
			}
			return client.os.major != null ? family + " " + client.os.major : family;
		}
		// Pour les autres OS (Mac OS X, Android, Chrome OS, Ubuntu, Linux...), on ne garde
		// que la famille : historiquement peu/pas de granularité utile était stockée pour
		// ces plateformes, et cela évite de fragmenter les statistiques inutilement.
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
