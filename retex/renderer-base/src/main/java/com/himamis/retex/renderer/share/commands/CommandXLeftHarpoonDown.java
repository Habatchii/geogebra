package com.himamis.retex.renderer.share.commands;

import com.himamis.retex.renderer.share.Atom;
import com.himamis.retex.renderer.share.TeXParser;
import com.himamis.retex.renderer.share.XArrowAtom;

public class CommandXLeftHarpoonDown extends Command1O1A {

	@Override
	public Atom newI(TeXParser tp, Atom a, Atom b) {
		return new XArrowAtom(b, a, XArrowAtom.Kind.LeftHarpoonDown);
	}

	@Override
	public Command duplicate() {
		CommandXLeftHarpoonDown ret = new CommandXLeftHarpoonDown();
		ret.hasopt = hasopt;
		ret.option = option;
		return ret;
	}

}
