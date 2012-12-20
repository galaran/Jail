package com.matejdro.bukkit.jail.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.jail.Jail;
import com.matejdro.bukkit.jail.JailPrisoner;
import com.matejdro.bukkit.jail.PrisonerManager;
import com.matejdro.bukkit.jail.Setting;
import com.matejdro.bukkit.jail.Settings;
import com.matejdro.bukkit.jail.Util;

public class JailCommand extends BaseCommand {	
	public JailCommand()
	{
		needPlayer = false;
		adminCommand = true;
		permission = "jail.command.jail";
	}
	
	public Boolean run(CommandSender sender, String[] args) {				
		if (args.length < 1)
		{
			Util.Message("Usage: /jail [Name] (time) (j:Jail name) (c:Cell name) (r:Reason) (m)", sender);
			return true;
		}
		
		if (Jail.zones.size() < 1)
		{
			Util.Message(Settings.getGlobalString(Setting.MessageNoJail), sender);
			return true;
		}
		
		//Initialize defaults
		String playerName = args[0].toLowerCase();
		int time = Settings.getGlobalInt(Setting.DefaultJailTime);
		String jailname = "";
		String cellname = "";
		String reason = "";
		Boolean muted = Settings.getGlobalBoolean(Setting.AutomaticMute);
		
		//Parse command line
		for (int i = 1; i < args.length; i++)
		{
			String line = args[i];
			
			if (Util.isInteger(line))
				time = Integer.parseInt(line);
			else if (line.startsWith("j:"))
				jailname = line.substring(2);
			else if (line.startsWith("c:"))
				cellname = line.substring(2);
			else if (line.equals("m"))
				muted = !muted;
			else if (line.startsWith("r:"))
			{
				if (line.startsWith("r:\""))
				{
					reason = line.substring(3);
					while (!line.endsWith("\""))
					{
						i++;
						if (i >= args.length)
						{
							Util.Message("Usage: /jail [Name] (t:time) (j:Jail name) (c:Cell name) (r:Reason) (m)", sender);
							return true;
						}
						
						line = args[i];
						if (line.endsWith("\""))
							reason += " " + line.substring(0, line.length() - 1);
						else
							reason += " " + line;
					}
				}
				else reason = line.substring(2);
				
				int maxReason = Settings.getGlobalInt(Setting.MaximumReasonLength);
				if (maxReason > 250) maxReason = 250; //DB Limit
				
				if (reason.length() > maxReason)
				{
					Util.Message(Settings.getGlobalString(Setting.MessageTooLongReason), sender);
					return true;
				}
				
			}
		}
		
		Player player = Util.getPlayer(playerName, true);
		
		if (player == null && !Util.playerExists(playerName))
		{
			Util.Message(Settings.getGlobalString(Setting.MessageNeverOnThisServer).replace("<Player>", playerName), sender);
			return true;
		}
		else if (player != null) playerName = player.getName().toLowerCase();
		
		
		
		
		JailPrisoner prisoner = new JailPrisoner(playerName, time * 6, jailname, cellname, false, "", reason, muted, "", sender instanceof Player ? ((Player) sender).getName() : "console", ""); 
		PrisonerManager.PrepareJail(prisoner, player);
		
		String message;
		if (player == null)
			message = Settings.getGlobalString(Setting.MessagePrisonerOffline);
		else
			message = Settings.getGlobalString(Setting.MessagePrisonerJailed);
		message = prisoner.parseTags(message);
		Util.Message(message, sender);
		return true;
		
	}

}
