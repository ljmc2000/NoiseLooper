package ie.delilahsthings.soothingloop;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static ie.delilahsthings.soothingloop.HtmlConstants.*;

public class CreditsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);
        WebView creditsView = this.findViewById(R.id.sound_credits);
        creditsView.setBackgroundColor(getResources().getColor(R.color.bg_color_light));

        try {
            creditsView.loadData(loadCredits(),"text/html","utf8");
        } catch (Exception e) {
            creditsView.loadData(getString(R.string.credits_error),"text/plain","utf8");
            //throw new RuntimeException(e);
        }
    }

    private String loadCredits() throws ParserConfigurationException, IOException, SAXException {
        InputStream creditsFile = getResources().openRawResource(R.raw.credits);
        InputStream creditsTemplate = getResources().openRawResource(R.raw.credits_template);
        String HEADER = getString(R.string.credits_header);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(creditsFile);
        Element node;

        Document credits = db.parse(creditsTemplate);
        Element header, body, item;

        header = credits.getElementById("page_header");
        header.setTextContent(getString(R.string.credits_label));

        NodeList developerCredits=document.getElementsByTagName("developer");
        header=credits.getElementById("developer_credits_header");
        header.setTextContent(String.format(HEADER,getString(R.string.developers)));
        body=credits.getElementById("developer_credits");
        for(int i=0; i<developerCredits.getLength(); i++) {
            node=(Element) developerCredits.item(i);
            developer(credits,body,node);
        }


        NodeList upstreamCredits=document.getElementsByTagName("upstream_developer");
        header=credits.getElementById("upstream_application_credits_header");
        header.setTextContent(String.format(HEADER,getString(R.string.upstream_application)));
        body=credits.getElementById("upstream_application_credits");
        for(int i=0; i<upstreamCredits.getLength(); i++) {
            node=(Element) upstreamCredits.item(i);
            developer(credits,body,node);
        }



        String Author = String.format(HEADER,getString(R.string.author));
        String Editor = String.format(HEADER,getString(R.string.editor));
        String Icon = String.format(HEADER,getString(R.string.icon));
        String License = String.format(HEADER,getString(R.string.license));
        String author, editor, icon, license;

        header=credits.getElementById("sounds_credits_header");
        header.setTextContent(String.format(HEADER,getString(R.string.sounds)));
        body=credits.getElementById("sounds_credits");
        NodeList sounds = document.getElementsByTagName("sound");

        int nameId;
        for(int i=0; i<sounds.getLength(); i++) {
            node = (Element) sounds.item(i);
            nameId = getResources().getIdentifier(node.getAttribute("id"), "string", getPackageName());

            author = node.getAttribute("author");
            editor = node.getAttribute("editor");
            icon = node.getAttribute("icon");
            license = node.getAttribute("license");

            item=credits.createElement(DIV);
            header=credits.createElement(A);
            header.setTextContent(getString(nameId));
            header.setAttribute(HREF,node.getAttribute("source"));
            item.setAttribute(CLASS,"block");
            item.appendChild(header);
            body.appendChild(item);

            if (!author.isEmpty())
                doubleIndent(credits, item, Author+author);
            if (!editor.isEmpty())
                doubleIndent(credits, item, Editor+editor);
            if (!icon.isEmpty())
                doubleIndent(credits, item, Icon+icon);
            if (!license.isEmpty())
                doubleIndent(credits, item, License+license);
        }

        return getStringFromDocument(credits);
    }

    private void developer(Document credits, Element parent, Element node) {
        String website = node.getAttribute("website");
        Element item, subitem;

        if(!website.isEmpty()) {
            item = credits.createElement(DIV);
            subitem = credits.createElement(A);
            subitem.setTextContent(node.getTextContent());
            subitem.setAttribute(HREF, website);
            item.appendChild(subitem);
            parent.appendChild(item);
        }

        else {
            item = credits.createElement(DIV);
            item.setTextContent(node.getTextContent());
            parent.appendChild(item);
        }
    }

    void doubleIndent(Document credits, Element parent, String content) {
        Element e = credits.createElement(DIV);
        e.setTextContent(content);
        e.setAttribute("class", "double_indented_block");
        parent.appendChild(e);
    }

    public static String getStringFromDocument(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        }
        catch (TransformerException ex) {
            return "";
        }
    }
}