package com.aspiresys.fp_micro_productservice.product;

public class ProductUtils {

    /**
     * Validates the product category.
     *
     * @param category the category to validate
     * @return true if the category is valid, false otherwise
     */
    public static boolean isValidCategory(String category) {
        return "smartphone".equalsIgnoreCase(category) || "clothes".equalsIgnoreCase(category);
    }

    /**
     * Validates the product stock.
     *
     * @param stock the stock to validate
     * @return true if the stock is valid, false otherwise
     */
    public static boolean isValidStock(int stock) {
        return stock >= 0;
    }
    /**
     * Validates the product price.
     *
     * @param price the price to validate
     * @return true if the price is valid, false otherwise
     */
    public static boolean isValidPrice(Double price) {
        return price != null && price >= 0;
    }
    /**
     * Validates the product name.
     *
     * @param name the name to validate
     * @return true if the name is valid, false otherwise
     */
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty();
    }
    /**
     * Validates the product image URL.
     *
     * @param imageUrl the image URL to validate
     * @return true if the image URL is valid, false otherwise
     */
    public static boolean isValidImageUrl(String imageUrl) {
        return imageUrl != null && !imageUrl.trim().isEmpty() && imageUrl.startsWith("http");
    }
    
    /**
     * Validates the entire product object.
     *
     * @param product the product to validate
     * @return true if the product is valid, false otherwise
     */
    public static TupleResponse<Boolean,String>   isAValidProduct(Product product) {
        if (product == null) {
            return new TupleResponse<>(false, "Product cannot be null");
        }
        if (!isValidCategory(product.getCategory())) {
            return new TupleResponse<>(false, "Invalid category: " + product.getCategory());
        }
        if (!isValidStock(product.getStock())) {
            return new TupleResponse<>(false, "Invalid stock: " + product.getStock());
        }
        if (!isValidPrice(product.getPrice())) {
            return new TupleResponse<>(false, "Invalid price: " + product.getPrice());
        }
        if (!isValidName(product.getName())) {
            return new TupleResponse<>(false, "Invalid name: " + product.getName());
        }
        if (!isValidImageUrl(product.getImageUrl())) {
            return new TupleResponse<>(false, "Invalid image URL: " + product.getImageUrl());
        }
        return new TupleResponse<>(true, "Product is valid");
    }

    public static class TupleResponse<T, U> {
        private T first;
        private U second;

        public TupleResponse(T first, U second) {
            this.first = first;
            this.second = second;
        }

        public T getFirst() {
            return first;
        }

        public U getSecond() {
            return second;
        }
    }

}
