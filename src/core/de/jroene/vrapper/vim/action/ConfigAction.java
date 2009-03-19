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
    };

}
