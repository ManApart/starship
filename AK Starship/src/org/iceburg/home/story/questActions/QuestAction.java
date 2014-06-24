package org.iceburg.home.story.questActions;

import org.iceburg.home.story.QuestEvent;
//TODO this class will be extended by specific actions that advance the quest when completed
public class QuestAction {
	QuestEvent parent;
	
	public void startAction() {
	}
	public void updateAction() {
	}
	public void endAction() {
		parent.incStep();
	}
}
