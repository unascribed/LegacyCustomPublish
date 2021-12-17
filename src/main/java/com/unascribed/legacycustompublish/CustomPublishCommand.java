package com.unascribed.legacycustompublish;

import java.io.IOException;
import java.net.InetAddress;
import cpw.mods.fml.common.network.FMLNetworkHandler;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.CommandBase;
import net.minecraft.src.CommandException;
import net.minecraft.src.EnumGameType;
import net.minecraft.src.ICommand;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.IntegratedServer;
import net.minecraft.src.IntegratedServerListenThread;
import net.minecraft.src.ServerListenThread;
import net.minecraft.src.ThreadLanServerPing;

public class CustomPublishCommand extends CommandBase {
	
	@Override
	public String getCommandName() {
		return "publish";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/"+getCommandName()+" [port] [allowCheats]";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length == 0) {
			String addr = MinecraftServer.D().shareToLAN(EnumGameType.SURVIVAL, false);
			if (addr != null) {
				notifyAdmins(sender, "commands.publish.started", addr);
			} else {
				notifyAdmins(sender, "commands.publish.failed");
			}
			return;
		}
		if (args.length > 2) {
			throw new CommandException("Too many arguments");
		}
		int port = parseIntBounded(sender, args[0], 1, 65535);
		boolean allowCheats = args.length > 1 && "true".equals(args[1]);
		MinecraftServer server = MinecraftServer.D();
		if (!(server instanceof IntegratedServer)) {
			notifyAdmins(sender, "commands.publish.failed");
			return;
		}
		IntegratedServer is = (IntegratedServer)server;
		ServerListenThread slt = ReflectionHelper.getPrivateValue(IntegratedServerListenThread.class, is.getServerListeningThread(), 4);
		if (slt == null) {
			try {
				slt = new ServerListenThread(is.getServerListeningThread(), (InetAddress)null, port);
				slt.start();
				ReflectionHelper.setPrivateValue(IntegratedServerListenThread.class, is.getServerListeningThread(), slt, 4);
			} catch (IOException var3) {
				var3.printStackTrace();
				notifyAdmins(sender, "commands.publish.failed");
				return;
			}
		}
		String addr;

		try {
			addr = FMLNetworkHandler.computeLocalHost().getHostAddress() + ":" + slt.getMyPort();
		} catch (IOException e) {
			addr = ""+slt.getMyPort();
		}
		
		if (allowCheats) {
			addr += " with cheats enabled";
		}

		System.out.println("Started on " + addr);
		ReflectionHelper.setPrivateValue(IntegratedServer.class, is, true, 4); // isPublic
		ThreadLanServerPing lanServerPing = new ThreadLanServerPing(is.aa(), addr);
		lanServerPing.start();
		ReflectionHelper.setPrivateValue(IntegratedServer.class, is, lanServerPing, 5); // lanServerPing
		is.getConfigurationManager().setGameType(EnumGameType.SURVIVAL);
		is.getConfigurationManager().setCommandsAllowedForAll(allowCheats);

		notifyAdmins(sender, "commands.publish.started", addr);
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof ICommand) return ((ICommand)o).getCommandName().compareTo(getCommandName());
		return -1;
	}
}
