package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.net.Uri;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public abstract class ProfileManager {

    final private static Pattern profileNameValidationPattern = Pattern.compile("\\s*(.*[\\S])\\s*");
    final private static String ProfileExtension = ".xml";
    final private static int ProfileExtensionLength = 4;

    static String getProfilePath()
    {
        return StaticContext.getAppContext().getFilesDir()+"/profiles/";
    }
    static File getProfileFile(String filename) {
        new File(getProfilePath()).mkdirs();
        return new File(getProfilePath()+filename+ProfileExtension);
    }
    static File getDefaultProfileFile() {
        return new File(StaticContext.getAppContext().getFilesDir()+"/default_profile"+ProfileExtension);
    }

    public static boolean deleteProfile(String profileName)
    {
        String path = getProfilePath()+"/"+profileName+ProfileExtension;
        return new File(path).delete();
    }

    public static boolean exportProfile(String exportedProfileName, Uri uri) {
        try {
            Context ctx = StaticContext.getAppContext();
            OutputStream out = ctx.getContentResolver().openOutputStream(uri);
            File in = getProfileFile(exportedProfileName);
            return saveProfile(out, loadProfile(in));
        }
        catch (IOException ex) {
            return false;
        }
    }

    public static boolean importProfile(String profileName, Uri uri) {
        try {
            Context ctx = StaticContext.getAppContext();
            File out = getProfileFile(profileName);
            InputStream in = ctx.getContentResolver().openInputStream(uri);
            return saveProfile(out, loadProfile(in));
        }
        catch (IOException ex) {
            return false;
        }
    }

    public static String[] listProfiles()
    {
        ArrayList<String> files = new ArrayList();
        String path = getProfilePath();
        for(String file: Util.safe_list(new File(path))) {
            files.add(file.substring(0, file.length() - ProfileManager.ProfileExtensionLength));
        }
        return files.toArray(new String[0]);
    }

    public static Profile loadProfile(String profileName) {
        return loadProfile(getProfileFile(profileName));
    }

    public static boolean saveProfile(String profileName, Profile profile) {
        return saveProfile(getProfileFile(profileName), profile);
    }

    public static Profile loadDefaultProfile() {
        return loadProfile(getDefaultProfileFile());
    }

    public static boolean saveDefaultProfile(Profile profile) {
        return saveProfile(getDefaultProfileFile(), profile);
    }

    public static String validateProfileName(CharSequence profileName) throws BadProfileNameException
    {
        Matcher matcher = profileNameValidationPattern.matcher(profileName);
        if(matcher.find())
            return matcher.group(1);
        else
            throw new BadProfileNameException();
    }

    public static class BadProfileNameException extends Exception {
        public BadProfileNameException() {
            super("Invalid Profile Name");
        }
    }

    public static class Profile extends HashMap<String, Integer> {
        Integer get(String key) {
            Integer i = super.get(key);
            return i==null?0:i;
        }
    }

    private static Profile loadProfile(InputStream profileXml) {
        try {
            Profile profile = new Profile();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(profileXml);
            NodeList items = document.getElementsByTagName("int");
            Element item;

            for(int i=0; i<items.getLength(); i++) {
                item=(Element) items.item(i);
                profile.put(item.getAttribute("name"), Integer.parseInt(item.getAttribute("value")));
            }

            return profile;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            return null;
        }
    }

    private static Profile loadProfile(File profileFile) {
        try {
            InputStream profileXml = new FileInputStream(profileFile);
            return loadProfile(profileXml);
        }
        catch (IOException ex) {
            return null;
        }
    }

    private static boolean saveProfile(OutputStream out, Profile profile) {
        if(profile==null) {
            return false;
        }

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.newDocument();
            Element xmlItem, map = document.createElement("map");
            document.appendChild(map);

            for(Map.Entry<String, Integer> item: profile.entrySet()) {
                xmlItem=document.createElement("int");
                xmlItem.setAttribute("name", item.getKey());
                xmlItem.setAttribute("value", item.getValue().toString());
                map.appendChild(xmlItem);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(out);

            transformer.transform(source, result);

            return true;
        }
        catch (ParserConfigurationException | TransformerException e) {
            return false;
        }
    }

    private static boolean saveProfile(File outFile, Profile profile) {
        try {
            OutputStream out = new FileOutputStream(outFile);
            return saveProfile(out, profile);
        }
        catch (IOException ex) {
            return false;
        }
    }
}
