import fileReading.TextReading;
import fileWriting.TextWriting;
import stringHandling.Parsing;

import java.util.ArrayList;

public class Process
{

    private ArrayList<String> lines, result;

    public Process(String path)
    {

        lines = new ArrayList<>();
        result = new ArrayList<>();

        clean(TextReading.read(path));
        enumDetect();
        constDetect();
        funDetect();

        TextWriting.write(result.get(0).substring(2) + ".md", result);
    }

    private void clean(ArrayList<String> allLines)
    {
        int loc = 0;

        while (loc < allLines.size())
        {
            if (allLines.get(loc).trim().startsWith("public") &&
            allLines.get(loc).trim().substring(7).trim().startsWith("class"))
            {
                String className = allLines.get(loc).trim().substring(7).trim().substring(6).trim();
                if (className.contains("{")) className = className.substring(0, className.indexOf("{"));

                result.add("# " + className);
                break;
            }

            loc++;
        }

        boolean flag = false;

        while (loc < allLines.size())
        {
            if (allLines.get(loc).startsWith("/*")) flag = true;
            if (allLines.get(loc).endsWith("*/")) flag = false;

            if (!allLines.get(loc).startsWith("//") && !flag &&
                    !allLines.get(loc).isEmpty() && allLines.get(loc) != null)
                lines.add(allLines.get(loc));

            loc++;
        }
    }

    private void enumDetect()
    {
        int loc = 0;

        while (loc < lines.size())
        {

            String line = lines.get(loc).replaceAll(" ", "");

            if (line.startsWith("publicenum") || line.startsWith("protectedenum") || line.startsWith("privateenum"))
            {
                int beginIndex;
                String access;

                if (line.startsWith("publicenum"))
                {
                    beginIndex = 11;
                    access = "public";
                }
                else if (line.startsWith("protectedenum"))
                {
                    beginIndex = 14;
                    access = "protected";
                }
                else
                {
                    beginIndex = 12;
                    access = "private";
                }

                String name = "## " + line.substring(beginIndex).trim();
                if (name.endsWith("{")) name = name.substring(0, name.length()-1);
                result.add(name);
                result.add("**access:** " + access + "\n\n**description:**\n");

                while (loc < lines.size())
                {
                    boolean flag = true;

                    String objs = lines.get(loc);

                    if (lines.get(loc).trim().equals("}")) break;
                    else if (lines.get(loc).trim().endsWith("}"))
                    {
                        objs = objs.substring(0, objs.length()-1);
                        flag = false;
                    }

                    String[] objList = objs.split(",");
                    for (String st: objList) if (!st.isEmpty() && !st.isBlank()) result.add("* " + st);

                    if (!flag) break;

                    loc++;
                }
            }

            loc++;
        }
    }

    private void constDetect()
    {
        ArrayList<String> constLines = new ArrayList<>();
        ArrayList<String> accessTypes = new ArrayList<>();

        int loc = 0;

        while (loc < lines.size())
        {
            if (lines.get(loc).trim().replaceAll(" ", "").startsWith("public" + result.get(0).substring(2)) ||
                    lines.get(loc).trim().replaceAll(" ", "").startsWith("protected" + result.get(0).substring(2)))
            {
                int init = lines.get(loc).indexOf("(") + 1;
                int fin = getLastIndex(lines.get(loc));

                constLines.add(lines.get(loc).substring(init, fin));

                if (lines.get(loc).trim().startsWith("public")) accessTypes.add("public");
                else accessTypes.add("protected");
            }

            loc++;
        }

        if (constLines.size() == 1)
        {
            result.add("## constructor");
            result.add("**access:** " + accessTypes + "\n");
            result.add("**description:**\n");
            result.add("**parameter(s):** " + constLines.get(0) + "\n");
        }
        else if (constLines.size() > 1)
        {
            for (int i = 0; i < constLines.size(); i++)
            {
                result.add("## constructor type-" + i);
                result.add("**access:** " + accessTypes + "\n");
                result.add("**description:**\n");
                result.add("**parameter(s):** " + constLines.get(i) + "\n");
            }
        }
    }
    private void funDetect()
    {
        int loc = 0;

        ArrayList<String> accessTypes = new ArrayList<>();
        ArrayList<String> params = new ArrayList<>();
        ArrayList<String> returnTypes = new ArrayList<>();
        ArrayList<String> isStatic = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Boolean> isSubclass = new ArrayList<>();

        while (loc < lines.size())
        {
            if (!isConst(lines.get(loc)) && isFun(lines.get(loc)))
            {
                String raw = lines.get(loc).replaceAll(" ", "");

                String access, name, returned, param;
                boolean flagStatic = false;

                if (raw.startsWith("public"))
                {
                    access = "public";
                    raw = raw.substring(6);
                }
                else if (raw.startsWith("protected"))
                {
                    access = "protected";
                    raw = raw.substring(9);
                }
                else
                {
                    access = "private";
                    raw = raw.substring(7);
                }

                if (raw.startsWith("static") && lines.get(loc).contains(" static "))
                {
                    flagStatic = true;
                    raw = raw.substring(6);
                }

                String[] pieces = lines.get(loc).split(" ");

                ArrayList<String> nonNullPieces = new ArrayList<>();
                for (String s: pieces) if (!s.isEmpty() && !s.isBlank() && !s.contains(" ")) nonNullPieces.add(s);

                if (flagStatic) returned = nonNullPieces.get(2);
                else returned = nonNullPieces.get(1);

                name = raw.substring(returned.length(), raw.indexOf("(")).trim();

                int init = lines.get(loc).indexOf("(") + 1;
                int fin = getLastIndex(lines.get(loc));

                param = lines.get(loc).substring(init, fin);
                if (param.isEmpty() || param.isBlank()) param = "void";


                if (names.contains(name))
                {
                    int index = names.indexOf(name);

                    accessTypes.set(index, accessTypes.get(index) + ";" + access);
                    params.set(index, params.get(index) + ";" + param);
                    returnTypes.set(index, returnTypes.get(index) + ";" + returned);
                    isStatic.set(index, isStatic.get(index) + ";" + flagStatic);
                    names.set(index, names.get(index) + ";" + name);
                    isSubclass.add(false);
                }
                else
                {
                    accessTypes.add(access);
                    params.add(param);
                    returnTypes.add(returned);
                    isStatic.add(Boolean.toString(flagStatic));
                    names.add(name);
                    isSubclass.add(false);
                }

                isSubclass.add(false);

            }

            else if (isSubclass(lines.get(loc)))
            {
                String raw = lines.get(loc).replaceAll(" ", "");

                if (raw.startsWith("public")) raw = raw.substring(6);
                else if (raw.startsWith("protected")) raw = raw.substring(9);
                else if (raw.startsWith("private")) raw = raw.substring(7);

                raw = raw.substring(5);

                if (raw.contains("{")) raw = raw.substring(0, raw.indexOf("{"));

                names.add(raw);
                isSubclass.add(true);
                accessTypes.add(null);
                params.add(null);
                returnTypes.add(null);
                isStatic.add(null);
            }


            loc++;
        }


        for (int i = 0; i < names.size(); i++)
        {
            if (isSubclass.get(i)) result.add("## " + names.get(i));
            else
            {
                if (accessTypes.get(i).contains(";"))
                {
                    String[] subAccess = accessTypes.get(i).split(";");
                    String[] subStatic = isStatic.get(i).split(";");
                    String[] subParameters = params.get(i).split(";");
                    String[] subReturn = returnTypes.get(i).split(";");

                    for (int j = 0; j < subAccess.length; j++)
                    {
                        result.add("### " + names.get(i) + " type-" + j);
                        result.add("**access:** " + subAccess[j] + "\n");
                        result.add("**static:** " + subStatic[j] + "\n");
                        result.add("**description:**\n");
                        result.add("**parameter(s):** " + subParameters[j] + "\n");
                        result.add("**returns:** " + subReturn[j] + "\n");
                    }
                }
                else
                {
                    result.add("### " + names.get(i));
                    result.add("**access:** " + accessTypes.get(i) + "\n");
                    result.add("**static:** " + isStatic.get(i) + "\n");
                    result.add("**description:**\n");
                    result.add("**parameter(s):** " + params.get(i) + "\n");
                    result.add("**returns:** " + returnTypes.get(i) + "\n");
                }
            }

        }

    }

    private boolean isSubclass(String line)
    {
        if (isClass(line))
        {
            line = line.replaceAll(" ", "");

            if (line.contains("publicclass" + result.get(0).substring(2)) ||
            line.contains("privateclass" + result.get(0).substring(2)))
                return true;
            else return false;
        }
        else return false;
    }

    private boolean isConst(String line)
    {
        if (line.trim().replaceAll(" ", "").startsWith("public" + result.get(0).substring(2)) ||
                line.trim().replaceAll(" ", "").startsWith("protected" + result.get(0).substring(2)))
            return true;

        else return false;
    }

    private boolean isFun(String line)
    {
        boolean flag = true;

        line = line.trim();

        if (!(line.startsWith("public ") || line.startsWith("protected ") || line.startsWith("private ")))
            flag = false;

        if (flag && !(line.contains("(") && line.contains(")"))) flag = false;

        if (flag && line.endsWith(";")) flag = false;

        return flag;
    }

    private boolean isClass(String line)
    {
        if ((line.startsWith("public ") || line.startsWith("protected ") || line.startsWith("private ")) &&
        line.contains(" class ") &&
                (line.replaceAll(" ", "").startsWith("publicclass") ||
                        line.replaceAll(" ", "").startsWith("protectedclass")))
            return true;

        else return false;
    }
    private int isBracket(boolean openBracket, String line)
    {
        int brackets = 0;

        char requested;
        if (openBracket) requested = '{';
        else requested = '}';

        ArrayList<Integer> locs = new ArrayList<>();

        for (int i = 0; i < line.length(); i++) if (line.charAt(i) == requested) locs.add(i);

        for (int l: locs) if (Parsing.isString(line, l, l)) brackets++;

        return brackets;
    }

    private Integer getLastIndex(String line)
    {
        ArrayList<Integer> indexes = new ArrayList<>();

        for (int i = 0; i < line.length(); i++)
        {
            if (line.charAt(i) == ')') indexes.add(i);
        }

        return indexes.get(indexes.size() - 1);
    }


}
