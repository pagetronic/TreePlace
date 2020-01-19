package live.page.android.threads;

import android.text.Html;
import android.text.Spanned;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import live.page.android.api.Json;

class PostParser {

    public static Spanned parse(String text, List<Json> docs, List<Json> links) {

        //TODO parse manually.

        text = text.replaceAll("\n\n", "</p><p>");
        text = text.replaceAll("\n", "<br/>");
        text = "<p>" + text + "</p>";
        text = text.replaceAll("\\[bold](.+?)\\[/bold]", "<strong>$1</strong>");
        text = text.replaceAll("\\[italic](.+?)\\[/italic]", "<em>$1</em>");
        text = text.replaceAll("\\[quote](.+?)\\[/quote]", "<blockquote>$1</blockquote>");

        Pattern pattern = Pattern.compile("\\[url=?([^]]+)?](.+?)\\[/url]", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String text_ = matcher.group(2).replace("#", "@~X@X~@");
            String url_ = (matcher.group(1) == null) ? text_ : matcher.group(1).replace("#", "@~X@X~@");
            text = text.replace(matcher.group(0), "<a href=\"" + url_.replace("#", "@~X@X~@") + "\">" + text_ + "</a>");
        }

        //TODO control in api
        // text = Jsoup.clean(text, new Whitelist().addTags("strong", "a", "p", "br", "em", "blockquote").addAttributes("a", "href"));
        text = text.replaceAll("<[a-z]></[a-z]>", "");

        text = text.replaceAll("([\n]+)", "\n").replace("  ", "\t").replace("\t ", "\t");
        text = text.replaceAll("[\t ]+<p></p>\n", "");

        //TODO
        //text = video(text);

        text = hasher(text);

        text = docs(text, docs != null ? docs : new ArrayList<Json>());

        text = text.replace("@~X@X~@", "#");

        text = dbtags(text, links);

        return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
    }

    private static String dbtags(String text, List<Json> links) {
        return text;
    }

    private static String hasher(String text) {
        return text;
    }

    private static String docs(String text, List<Json> docs) {
        return text;
    }
}
