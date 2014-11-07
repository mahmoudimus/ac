# Get list of available connect add-ons from marketplace
# Install on the OD instance via UPM
# Ensure it's installed and active
# To run:
# 1. Ensure you have 'rest-client' gem installed, you can do "sudo gem install rest-client" to be sure
# 2. Do "ruby install-all-connect-addons.rb" and it will install all available JIRA and Confluence connect add-ons


require 'rest-client'

INSTANCE_BASE_URL = <put isntance url here>
USERNAME = <put username here>
PASSWORD = <put password here>
UPM_REST_API_BASE = "/rest/plugins/1.0/"



def get_descriptor(plugin)
    descriptor = ""
    plugin["version"]["links"].each do |link|
        if link && link["rel"].eql?("descriptor")
            descriptor = link["href"]
        end
    end
    descriptor
end

def check_add_on_installed(add_on, rest_client)
    max_tries = 30
    counter = 0
    response_code = 0
    while response_code != 200 && counter < max_tries do 
        begin
            response_code = rest_client["#{add_on[:key]}-key"].get.code            
        rescue Exception => e
            sleep 1
        end        
        counter+=1
    end
    if counter >=29
        puts "FAILED to install #{add_on[:name]}"            
    end    
end

def get_available_connect_add_ons(product)
    marketplace = RestClient::Resource.new("https://marketplace.atlassian.com/rest/1.0")
    add_ons = []
    plugins = JSON.parse(marketplace["/plugins/app/#{product}?hosting=ondemand&addOnType=three&limit=500"].get)["plugins"]
    plugins.each do |plugin| 
        add_ons << {:name => plugin["name"], :descriptor => get_descriptor(plugin), :key => plugin["pluginKey"]}        
    end
    add_ons
end

def install_add_ons(add_ons, rest_client)                
    add_ons.each do |add_on|
        upm_token = rest_client.get.headers[:upm_token]
        puts "Installing Add-on #{add_on[:name]} with key #{add_on[:key]}"
        payload = {:pluginUri => add_on[:descriptor]}
        puts rest_client["?jar=false&token=#{upm_token}"]
        rest_client["?jar=false&token=#{upm_token}"].post(payload.to_json, :content_type => 'application/vnd.atl.plugins.install.uri+json')
        # Check if it's installed before moving to next
        check_add_on_installed(add_on, rest_client)
    end    
end


jira_add_ons = get_available_connect_add_ons("jira")
confluence_add_ons = get_available_connect_add_ons("confluence")


jira = RestClient::Resource.new(INSTANCE_BASE_URL + UPM_REST_API_BASE, :user => USERNAME, :password => PASSWORD)
confluence = RestClient::Resource.new(INSTANCE_BASE_URL + "/wiki" + UPM_REST_API_BASE, :user => USERNAME, :password => PASSWORD)
install_add_ons(jira_add_ons, jira)
install_add_ons(confluence_add_ons, confluence)
