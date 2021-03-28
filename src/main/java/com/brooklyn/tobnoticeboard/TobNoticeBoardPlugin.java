/*
 * Copyright (c) 2021, Brooklyn <https://github.com/Broooklyn>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.brooklyn.tobnoticeboard;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Friend;
import net.runelite.api.FriendsChatMember;
import net.runelite.api.Ignore;
import net.runelite.api.NameableContainer;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "ToB Notice Board",
	description = "Highlight friends and clan members on the Theatre of Blood Notice Board",
	tags = "theatre, theater, pvm, combat, party, friend, clan, cc, fc, friendschat, clanchat, raids, hub, brooklyn"
)
public class TobNoticeBoardPlugin extends Plugin
{
	private static final int DEFAULT_RGB = 0xff981f;

	@Inject
	private Client client;

	@Inject
	private TobNoticeBoardConfig config;

	@Inject
	private ClientThread clientThread;

	@Override
	public void startUp()
	{
		setNoticeBoard();
	}

	@Override
	public void shutDown()
	{
		unsetNoticeBoard();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("tobnoticeboard"))
		{
			setNoticeBoard();
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		clientThread.invokeLater(() ->
		{
			if (widgetLoaded.getGroupId() == 364)
			{
				setNoticeBoard();
			}
		});
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (event.getScriptId() == ScriptID.FRIENDS_UPDATE || event.getScriptId() == ScriptID.IGNORE_UPDATE)
		{
			setNoticeBoard();
		}
	}

	private void setNameColors(int friendColor, int clanColor, int ignoreColor)
	{
		for (int childID = 17; childID < 62; ++childID)
		{
			Widget noticeBoard = client.getWidget(364, childID);

			if (noticeBoard != null && noticeBoard.getName() != null && noticeBoard.getChildren() != null)
			{
				for (Widget noticeBoardChild : noticeBoard.getChildren())
				{
					if (noticeBoardChild.getIndex() == 3)
					{
						NameableContainer<Ignore> ignoreContainer = client.getIgnoreContainer();
						NameableContainer<Friend> friendContainer = client.getFriendContainer();

						if (ignoreContainer.findByName(Text.removeTags(noticeBoard.getName())) != null)
						{
							noticeBoardChild.setTextColor(config.highlightIgnored() ? ignoreColor : DEFAULT_RGB);
						}
						else if (friendContainer.findByName(Text.removeTags(noticeBoard.getName())) != null)
						{
							noticeBoardChild.setTextColor(config.highlightFriends() ? friendColor : DEFAULT_RGB);
						}
						else if (client.getFriendsChatManager() != null)
						{
							for (FriendsChatMember member : client.getFriendsChatManager().getMembers())
							{
								if (Text.toJagexName(member.getName()).equals(Text.removeTags(noticeBoard.getName())))
								{
									noticeBoardChild.setTextColor(config.highlightClan() ? clanColor : DEFAULT_RGB);
								}
							}
						}
					}
				}
			}
		}
	}

	private void setNoticeBoard()
	{
		setNameColors(config.friendColor().getRGB(), config.clanColor().getRGB(), config.ignoredColor().getRGB());
	}

	private void unsetNoticeBoard()
	{
		setNameColors(DEFAULT_RGB, DEFAULT_RGB, DEFAULT_RGB);
	}

	@Provides
	TobNoticeBoardConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TobNoticeBoardConfig.class);
	}
}
