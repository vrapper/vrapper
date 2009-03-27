package de.jroene.vrapper.vim.commandline;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.Search;
import de.jroene.vrapper.vim.VimConstants;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimUtils;
import de.jroene.vrapper.vim.action.SearchMove;
import de.jroene.vrapper.vim.token.AbstractMove;
import de.jroene.vrapper.vim.token.Token;
import de.jroene.vrapper.vim.token.TokenException;

public class SearchMode extends AbstractCommandMode {

    private static final Pattern AFTER_SEARCH_PATTERN = Pattern.compile("(e|b)?\\+?(-?\\d)?");

    public SearchMode(VimEmulator vim) {
        super(vim);
    }

    @Override
    public void parseAndExecute(String first, String command) {
        Search search = createSearch(first, command);
        vim.getRegisterManager().setSearch(search);
        Token t = new SearchMove(false);
        vim.getPlatform().setSpace(t.getSpace());
        try {
            t.evaluate(vim, null);
            t.getAction().execute(vim);
        } catch (TokenException e) {
            // not found, do nothing
        }
    }

    private Search createSearch(String first, String command) {
        boolean backward = first.equals(VimConstants.BACKWARD_SEARCH_CHAR);
        StringTokenizer nizer = new StringTokenizer(command, first);
        StringBuilder sb = new StringBuilder();
        while (nizer.hasMoreTokens()) {
            String token = nizer.nextToken();
            sb.append(token);
            if (token.endsWith(VimConstants.ESCAPE_CHAR)) {
                sb.replace(sb.length()-1, sb.length(), first);
            } else {
                break;
            }
        }
        String keyword = sb.toString();
        String afterSearch = nizer.hasMoreTokens() ? nizer.nextToken() : "";
        Token token = createAfterSearchToken(keyword, afterSearch);
        int offset = token instanceof OffsetToken ? ((OffsetToken)token).offset : 0;
        Search search = new Search(keyword, backward, false, token, offset);
        return search;
    }

    private Token createAfterSearchToken(String keyword, String afterSearch) {
        Matcher m = AFTER_SEARCH_PATTERN.matcher(afterSearch);
        String group;
        if(!m.find() || VimUtils.isBlank(afterSearch)) {
            return null;
        }
        group = m.group(2);
        int offset = VimUtils.isBlank(group) ? 0 : Integer.parseInt(group);
        group = m.group(1);
        if (VimUtils.isBlank(group)) {
            return new LineOffsetToken(offset);
        } else if (group.equals("e")) {
            offset += keyword.length()-1;
        }
        return offset != 0 ? new OffsetToken(offset) : null;
    }

    public static class LineOffsetToken extends AbstractMove {

        private final int offset;

        public LineOffsetToken(int offset) {
            super();
            this.offset = offset;
        }

        @Override
        public int calculateTarget(VimEmulator vim, Token next) {
            Platform p = vim.getPlatform();
            LineInformation currLine = p.getLineInformation();
            int number = currLine.getNumber()+offset;
            number = Math.max(number, 0);
            number = Math.min(number, p.getNumberOfLines()-1);
            return p.getLineInformation(number).getBeginOffset();
        }

        @Override
        public boolean isHorizontal() {
            return false;
        }
    }

    public static class OffsetToken extends AbstractMove {

        private final int offset;

        public OffsetToken(int offset) {
            super();
            this.offset = offset;
        }

        @Override
        public int calculateTarget(VimEmulator vim, Token next) {
            Platform p = vim.getPlatform();
            return VimUtils.calculatePositionForOffset(p, p.getPosition(), offset);
        }

        @Override
        public boolean isHorizontal() {
            return true;
        }
    }

}
