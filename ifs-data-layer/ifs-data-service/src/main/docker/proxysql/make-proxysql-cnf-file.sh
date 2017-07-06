#/bin/bash

set -e

# A function to generate a set of query_rules for proxysql to rewrite data as it is being selected by mysqldump.
# This takes rules defined in files in the /dump/rewrites folder and builds a set of proxysql configuration to apply
# those rewrites.  These rules will be written to /dump/query_rules
function generate_query_rules_for_proxysql() {

    files=(/dump/rewrites/*)

    # for each file within the /dump/rewrites folder
    for i in "${files[@]}"; do

        # the table name is the name of the file e.g. "user"
        table_name=$(echo $i | sed "s/.*\///" | sed "s/.*\///" | sed "s/\..*//")

        # the column_array is an array of the column names that are rewrite candidates for this table e.g. "first_name"
        mapfile -t column_array < <(sed 's/^\(.*\)|.*$/\1/g' $i)

        # the column_rewrite_array is an array of the rewrite rules against each corresponding column name in the column_array array e.g. "CONCAT(first_name, 'XXX')"
        mapfile -t column_rewrite_array < <(sed 's/^.*|\(.*\)$/\1/g' $i)

        # a query to find the base select statement that we wish mysqldump to issue against this table when running a dump of its data
        full_select_statement_query="SELECT CONCAT('SELECT SQL_NO_CACHE ', \
            (SELECT GROUP_CONCAT(t.col) FROM \
               (SELECT COLUMN_NAME as col FROM INFORMATION_SCHEMA.COLUMNS \
                WHERE TABLE_NAME = '$table_name' \
                AND TABLE_SCHEMA = '$DB_NAME') t \
             WHERE t.col IS NOT NULL) , ' FROM $table_name' );"

        # the base select statement that we wish mysqldump to issue against this table when running a dump of its data
        full_select_statement_result=$(mysql -h$DB_HOST -u$DB_USER -p$DB_PASS -P$DB_PORT $DB_NAME -N -s -e "$full_select_statement_query")

        # now we replace every column name that we wish to replace with its replacement i.e. replace every entry from column_array (e.g. "user") with its rewrite from column_rewrite_array (e.g. "CONCAT(first_name, 'XXX')")
        replacement_pattern=$full_select_statement_result
        for j in "${!column_array[@]}"; do
            replacement_pattern=$( echo $replacement_pattern | sed "s/${column_array[j]}/${column_rewrite_array[j]}/g" )
        done

        # and finally output this table's rewrite rule to /dump/query_rules
        if [ "$((j))" -gt "1" ]; then
           echo '    ,' >> /dump/query_rules
        fi

        rule_id=$j
        echo '    {' >> /dump/query_rules
        echo "        rule_id=$rule_id" >> /dump/query_rules
        echo '        active=1' >> /dump/query_rules
        match_pattern="^SELECT /\*!40001 SQL_NO_CACHE \*/ \* FROM \`$table_name\`"
        echo "        match_pattern=\"$match_pattern\"" >> /dump/query_rules
        echo "        replace_pattern=\"$replacement_pattern\"" >> /dump/query_rules
        echo '        destination_hostgroup=0' >> /dump/query_rules
        echo '        apply=1' >> /dump/query_rules
        echo '    }' >> /dump/query_rules

    done
}

# a function to replace the <<QUERY_RULES>> token in proxysql.cnf with the rewrite rules stored within /dump/query_rules
function inject_query_rules_into_proxysql_cnf() {
    sed -i "/<<QUERY_RULES>>/{
        s/<<QUERY_RULES>>//g
        r /dump/query_rules
    }" /etc/proxysql.cnf
}

# a function to replace database configuration replacement tokens in proxysql.cnf with real values
function inject_db_configuration_into_proxysql_cnf() {
    sed -i "s#<<DB_USER>>#$DB_USER#g;s#<<DB_PASS>>#$DB_PASS#g;s#<<DB_NAME>>#$DB_NAME#g;s#<<DB_HOST>>#$DB_HOST#g;s#<<DB_PORT>>#$DB_PORT#g" /etc/proxysql.cnf
}

# the entrypoint into this script
generate_query_rules_for_proxysql
inject_query_rules_into_proxysql_cnf
inject_db_configuration_into_proxysql_cnf