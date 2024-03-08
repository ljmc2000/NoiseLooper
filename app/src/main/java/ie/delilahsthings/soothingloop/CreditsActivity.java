package ie.delilahsthings.soothingloop;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

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

        NodeList otherCredits=document.getElementsByTagName("other");
        credits+=String.format(header,getString(R.string.other_credits))+"\n";
        for(int i=0; i<otherCredits.getLength(); i++) {
            node=(Element) otherCredits.item(i);
            credits+=Tab+node.getTextContent()+"\n";
        }
        credits+="\n";

        String Author = String.format(header,getString(R.string.author));
        String Editor = String.format(header,getString(R.string.editor));
        String License = String.format(header,getString(R.string.license));
        String author, editor, license;

        credits+=String.format(header,getString(R.string.sounds))+"\n";
        NodeList sounds = document.getElementsByTagName("sound");

        int nameId;
        for(int i=0; i<sounds.getLength(); i++) {
            node = (Element) sounds.item(i);
            nameId = getResources().getIdentifier(node.getAttribute("name"), "string", getPackageName());
            credits += (Tab + String.format(header, getString(nameId)) + "\n");
            author = node.getAttribute("author");
            editor = node.getAttribute("editor");
            license = node.getAttribute("license");
            if (!author.isEmpty())
                credits += (Tab2 + Author + author + "\n");
            if (!editor.isEmpty())
                credits += (Tab2 + Editor + editor + "\n");
            if (!license.isEmpty())
                credits += (Tab2 + License + license + "\n");
        }

        return credits;
    }
}