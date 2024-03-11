package ie.delilahsthings.soothingloop;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class CreditsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);
        TextView creditsView = this.findViewById(R.id.sound_credits);

        try {
            creditsView.setText(loadCredits());
        } catch (Exception e) {
            creditsView.setText(getString(R.string.credits_error));
            //throw new RuntimeException(e);
        }
    }

    private String loadCredits() throws ParserConfigurationException, IOException, SAXException {
        InputStream creditsFile = getResources().openRawResource(R.raw.credits);
        String credits = "";
        String header = getString(R.string.credits_header);
        String Tab="    ";
        String Tab2="        ";

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(creditsFile);
        Element node;

        NodeList developerCredits=document.getElementsByTagName("developer");
        credits+=String.format(header,getString(R.string.developers))+"\n";
        for(int i=0; i<developerCredits.getLength(); i++) {
            node=(Element) developerCredits.item(i);
            credits+=Tab+node.getTextContent()+"\n";
        }
        credits+="\n";

        NodeList upstreamCredits=document.getElementsByTagName("upstream_developer");
        credits+=String.format(header,getString(R.string.upstream_application))+"\n";
        for(int i=0; i<upstreamCredits.getLength(); i++) {
            node=(Element) upstreamCredits.item(i);
            credits+=Tab+node.getTextContent()+"\n";
        }
        credits+="\n";

        String Author = String.format(header,getString(R.string.author));
        String Editor = String.format(header,getString(R.string.editor));
        String Icon = String.format(header,getString(R.string.icon));
        String License = String.format(header,getString(R.string.license));
        String author, editor, icon, license;

        credits+=String.format(header,getString(R.string.sounds))+"\n";
        NodeList sounds = document.getElementsByTagName("sound");

        int nameId;
        for(int i=0; i<sounds.getLength(); i++) {
            node = (Element) sounds.item(i);
            nameId = getResources().getIdentifier(node.getAttribute("id"), "string", getPackageName());
            credits += (Tab + String.format(header, getString(nameId)) + "\n");
            author = node.getAttribute("author");
            editor = node.getAttribute("editor");
            icon = node.getAttribute("icon");
            license = node.getAttribute("license");
            if (!author.isEmpty())
                credits += (Tab2 + Author + author + "\n");
            if (!editor.isEmpty())
                credits += (Tab2 + Editor + editor + "\n");
            if (!icon.isEmpty())
                credits += (Tab2 + Icon + icon + "\n");
            if (!license.isEmpty())
                credits += (Tab2 + License + license + "\n");
        }

        return credits;
    }
}