package net.lapasa.rfdhotdealswidget.model;

import net.lapasa.rfdhotdealswidget.model.entities.DealWatchRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Spans
{
    private Pattern pattern;
    private String srcStr;
    public List<SpanPair> pairs = new ArrayList<>();

    public Spans(String srcStr, String targetStr, long type)
    {
        this.srcStr = srcStr;

        if (type == DealWatchRecord.FILTER_AND)
        {
            conjunctionSearch(targetStr);
        }
        else if (type == DealWatchRecord.FILTER_OR)
        {
            this.pattern = Pattern.compile(getDisjunctionRegEx(targetStr), Pattern.CASE_INSENSITIVE);
            search();
        }
        else if (type == DealWatchRecord.FILTER_EXACT)
        {
            this.pattern = Pattern.compile(targetStr, Pattern.CASE_INSENSITIVE);
            search();
        }

    }

    /**
     * If all individual terms match, then this is a true AND match
     *
     * @param targetStr
     */
    private void conjunctionSearch(String targetStr)
    {
        String[] terms = prepareStrings(targetStr);
        boolean isMatch = true;
        for (int i = 0; i < terms.length; i++)
        {
            String term = terms[i];
            Pattern p = Pattern.compile(term, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(srcStr);

            while (m.find())
            {
                pairs.add(new SpanPair(m.start(), m.end() - 1)); // Note: Matcher.end() returns the character after
            }

            if (pairs.size() == 0)
            {
                isMatch = false;
                break;
            }
        }
    }

    public static String[] prepareStrings(String targetStr)
    {
        // From http://stackoverflow.com/a/13307549/855984
        targetStr = targetStr.replaceAll("\\s+", " ").replaceAll(",+", ",").replaceAll("( , )+|( ,)+|(, )+", ",").replaceAll(",+", ",");
        return targetStr.split(",");

    }

    private String getConjunctionRegEx(String targetStr)
    {
        // From - http://stackoverflow.com/a/470602/855984
        // /^(?=.*\bword1\b)(?=.*\bword2\b)(?=.*\bword3\b).*$/m
        String[] terms = prepareStrings(targetStr);
        StringBuilder sb = new StringBuilder();

        sb.append("/^");

        for (int i = 0; i < terms.length; i++)
        {
            //            sb.append("(?=.*\\b");
            sb.append("(?=");
            sb.append(terms[i]);
            sb.append(")");
            //            sb.append("\\b)");
        }

        sb.append(".*$/m");

        return sb.toString();
    }

    public String getDisjunctionRegEx(String targetStr)
    {
        String[] terms = prepareStrings(targetStr);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < terms.length; i++)
        {
            sb.append(terms[i]);

            if (i != (terms.length - 1))
            {
                sb.append("|");
            }
        }

        return sb.toString();

    }

    private void search()
    {
        Matcher m = pattern.matcher(srcStr);
        while (m.find())
        {
            pairs.add(new SpanPair(m.start(), m.end() - 1)); // Note: Matcher.end() returns the character after
        }

    }

    public String getKeywords()
    {
        return srcStr;
    }


}
