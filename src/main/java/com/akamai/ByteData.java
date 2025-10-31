package com.akamai;

import java.util.List;

public class ByteData {
    private boolean is_test;
    private List<BillEntry> bill_entry_list;

    public boolean isIs_test() {
        return is_test;
    }

    public void setIs_test(boolean is_test) {
        this.is_test = is_test;
    }

    public List<BillEntry> getBill_entry_list() {
        return bill_entry_list;
    }

    public void setBill_entry_list(List<BillEntry> bill_entry_list) {
        this.bill_entry_list = bill_entry_list;
    }

    public static class BillEntry {
        private String bill_region;
        private String bill_cycle;
        private String good_category;
        private String instance_type;
        private String instance_id;
        private String ninety_five_time;
        private String amount_number;
        private String amount_unit;

        //只是输出给产品看，不上报的字段
        private transient String token;

        public String getBill_region() {
            return bill_region;
        }

        public void setBill_region(String bill_region) {
            this.bill_region = bill_region;
        }

        public String getBill_cycle() {
            return bill_cycle;
        }

        public void setBill_cycle(String bill_cycle) {
            this.bill_cycle = bill_cycle;
        }

        public String getGood_category() {
            return good_category;
        }

        public void setGood_category(String good_category) {
            this.good_category = good_category;
        }

        public String getInstance_type() {
            return instance_type;
        }

        public void setInstance_type(String instance_type) {
            this.instance_type = instance_type;
        }

        public String getInstance_id() {
            return instance_id;
        }

        public void setInstance_id(String instance_id) {
            this.instance_id = instance_id;
        }

        public String getNinety_five_time() {
            return ninety_five_time;
        }

        public void setNinety_five_time(String ninety_five_time) {
            this.ninety_five_time = ninety_five_time;
        }

        public String getAmount_number() {
            return amount_number;
        }

        public void setAmount_number(String amount_number) {
            this.amount_number = amount_number;
        }

        public String getAmount_unit() {
            return amount_unit;
        }

        public void setAmount_unit(String amount_unit) {
            this.amount_unit = amount_unit;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public String toCsvData() {
            String tokenOut = token.substring(token.length() - 5);
            return tokenOut + "," + bill_region + "," + bill_cycle + "," + good_category + "," + instance_type + "," + instance_id + "," + ninety_five_time + "," + amount_number + "," + amount_unit;
        }
    }
}
