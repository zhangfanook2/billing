package com.akamai;


import java.util.List;

public class LiNode {

    public static class InvoiceId {
        private int page;
        private int pages;
        private int results;
        private List<Data> data;

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getPages() {
            return pages;
        }

        public void setPages(int pages) {
            this.pages = pages;
        }

        public int getResults() {
            return results;
        }

        public void setResults(int results) {
            this.results = results;
        }

        public List<Data> getData() {
            return data;
        }

        public void setData(List<Data> data) {
            this.data = data;
        }

        public static class Data {
            private long id;
            private String date;

            public long getId() {
                return id;
            }

            public void setId(long id) {
                this.id = id;
            }

            public String getDate() {
                return date;
            }

            public void setDate(String date) {
                this.date = date;
            }
        }
    }

    public static class Items {
        private int page;
        private int pages;
        private int results;
        private List<Data> data;

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getPages() {
            return pages;
        }

        public void setPages(int pages) {
            this.pages = pages;
        }

        public int getResults() {
            return results;
        }

        public void setResults(int results) {
            this.results = results;
        }

        public List<Data> getData() {
            return data;
        }

        public void setData(List<Data> data) {
            this.data = data;
        }

        public static class Data {
            private String label;
            private Long quantity;
            private String region;

            //落地原始数据
            private String unit_price;
            private Double amount;
            private Double tax;
            private Double total;
            private String from;
            private String to;
            private String type;


            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }

            public Long getQuantity() {
                return quantity;
            }

            public void setQuantity(Long quantity) {
                this.quantity = quantity;
            }

            public String getRegion() {
                return region;
            }

            public void setRegion(String region) {
                this.region = region;
            }

            public String getUnit_price() {
                return unit_price;
            }

            public void setUnit_price(String unit_price) {
                this.unit_price = unit_price;
            }

            public Double getAmount() {
                return amount;
            }

            public void setAmount(Double amount) {
                this.amount = amount;
            }

            public Double getTax() {
                return tax;
            }

            public void setTax(Double tax) {
                this.tax = tax;
            }

            public Double getTotal() {
                return total;
            }

            public void setTotal(Double total) {
                this.total = total;
            }

            public String getFrom() {
                return from;
            }

            public void setFrom(String from) {
                this.from = from;
            }

            public String getTo() {
                return to;
            }

            public void setTo(String to) {
                this.to = to;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }
        }
    }

}
