/*
 * Copyright (c) 2025.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */

package labs.pm.app;

import labs.pm.data.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;


/**
 * {@code Shop} class represents an application that manages Products
 * @version 1.0
 * @author AymanElMikh
 */
public class Shop {
    public static void main(String[] args) {
        Product p1 = ProductManager.createProduct(1, "Bob", BigDecimal.valueOf(123.22), Rating.ONE_STAR, LocalDate.now());
        Product p2 = ProductManager.createProduct(1, "Bob", BigDecimal.valueOf(233.221), Rating.ONE_STAR);

        System.out.println(p1.getClass());
        System.out.println(p2.getClass());
    }

}