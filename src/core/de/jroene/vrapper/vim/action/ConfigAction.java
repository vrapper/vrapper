package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.VimEmulator;

public enum ConfigAction implements Action {

    GLOBAL_REGISTERS {
        public void execute(VimEmulator vim) {
            vim.useGlobalRegisters();
        }
    },
    LOCAL_REGISTERS {
        public void execute(VimEmulator vim) {
            vim.useLocalRegisters();
        }
    },
    AUTO_INDENT {
        public void execute(VimEmulator vim) {
            vim.getVariables().setAutoIndent(true);
        }
    },
    NO_AUTO_INDENT {
        public void execute(VimEmulator vim) {
            vim.getVariables().setAutoIndent(false);
        }
    },
    LINE_WISE_MOUSE_SELECTION {
        public void execute(VimEmulator vim) {
            vim.getPlatform().setLineWiseMouseSelection(true);
        }
    },
    NO_LINE_WISE_MOUSE_SELECTION {
        public void execute(VimEmulator vim) {
            vim.getPlatform().setLineWiseMouseSelection(false);
        }
    },
    START_OF_LINE {
        public void execute(VimEmulator vim) {
            vim.getVariables().setStartOfLine(true);
        }
    },
    NO_START_OF_LINE {
        public void execute(VimEmulator vim) {
            vim.getVariables().setStartOfLine(false);
        }
    };

}
