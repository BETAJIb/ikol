/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.handler.itemhandlers;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.datatables.csv.RecipeTable;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.RecipeList;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class Recipes implements IItemHandler
{
	private final int[] ITEM_IDS;
	
	public Recipes()
	{
		final RecipeTable rc = RecipeTable.getInstance();
		ITEM_IDS = new int[rc.getRecipesCount()];
		for (int i = 0; i < rc.getRecipesCount(); i++)
		{
			ITEM_IDS[i] = rc.getRecipeList(i).getRecipeId();
		}
	}
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		if (!(playable instanceof PlayerInstance))
		{
			return;
		}
		
		final PlayerInstance player = (PlayerInstance) playable;
		if (!Config.IS_CRAFTING_ENABLED)
		{
			player.sendMessage("Crafting is disabled, you cannot register this recipe.");
			return;
		}
		
		final RecipeList rp = RecipeTable.getInstance().getRecipeByItemId(item.getItemId());
		if (player.hasRecipeList(rp.getId()))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.RECIPE_ALREADY_REGISTERED));
		}
		else if (rp.isDwarvenRecipe())
		{
			if (player.hasDwarvenCraft())
			{
				if (rp.getLevel() > player.getDwarvenCraft())
				{
					// can't add recipe, becouse create item level too low
					player.sendPacket(new SystemMessage(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER));
				}
				else if (player.getDwarvenRecipeBook().length >= player.GetDwarfRecipeLimit())
				{
					// Up to $s1 recipes can be registered.
					SystemMessage sm = new SystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER);
					sm.addNumber(player.GetDwarfRecipeLimit());
					player.sendPacket(sm);
				}
				else
				{
					player.registerDwarvenRecipeList(rp);
					player.destroyItem("Consume", item.getObjectId(), 1, null, false);
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_ADDED);
					sm.addString(item.getItemName());
					player.sendPacket(sm);
				}
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT));
			}
		}
		else if (player.hasCommonCraft())
		{
			if (rp.getLevel() > player.getCommonCraft())
			{
				// can't add recipe, becouse create item level too low
				player.sendPacket(new SystemMessage(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER));
			}
			else if (player.getCommonRecipeBook().length >= player.GetCommonRecipeLimit())
			{
				// Up to $s1 recipes can be registered.
				SystemMessage sm = new SystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER);
				sm.addNumber(player.GetCommonRecipeLimit());
				player.sendPacket(sm);
			}
			else
			{
				player.registerCommonRecipeList(rp);
				player.destroyItem("Consume", item.getObjectId(), 1, null, false);
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_ADDED);
				sm.addString(item.getItemName());
				player.sendPacket(sm);
			}
		}
		else
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT));
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
