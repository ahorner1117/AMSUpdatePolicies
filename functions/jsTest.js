/**
 * 
 */
/**
 * 
 */const axios = require("axios");
var { google } = require("googleapis");
var serviceAccount = require("../zohoio-firebase-adminsdk-lbfc8-7f0769d96c.json");
//const axios = require("axios");



exports.handler = (event, context, callback) => {
    console.log("handling");

    var accessToken = 0;

    // Define the required scopes.
    var scopes = [
        "https://www.googleapis.com/auth/userinfo.email",
        "https://www.googleapis.com/auth/firebase.database"
    ];

    // Authenticate a JWT client with the service account.
    var jwtClient = new google.auth.JWT(
        serviceAccount.client_email,
        null,
        serviceAccount.private_key,
        scopes
    );

    console.log("event = \n" + event);
    var methodtype = event.httpMethod;

    console.log("method type = \n" + methodtype);

    var item = JSON.parse(event.body);
    console.log("item = " + item);

    // Make sure all the fields have values
    if (item.policynumber && item.premium && item.fName && item.carrier) {
        console.log("all clear");
    }
    else {
        console.log("the policy owner, premium, or number is missing: ", item);
        return;
    }

    var d = new Date();

    if (item.key !== "zohoAPI" + d.getDate()) {
        console.log("returning... sike! but check out this error");
        //return;
    } else {
        console.log("item.key  = " + item.key);
    }

    // Initializing the sales person name and id
    let init = {
        name: item.fName + ' ' + item.lName,
        id: item.id,
    };

    console.log(init);

    let policyinfo = {
        policynumber: item.policynumber,
        premium: item.premium,
        carrier: item.carrier,
        owner: item.fName + ' ' + item.lName,
        month: d.getMonth(),
        day: d.getDate(),
        year: d.getFullYear(),
        time: d.getHours() + ':' + d.getMinutes(),
        customerPolicyType: item.customerPolicyType
    };
    //console.log('help');


    // Checking if auth token is valid, if not go to "doWork()" and log "pt 2"
    jwtClient.authorize(function (error, tokens) {
        if (error) {
            console.log("Error making request to generate access token:", error);
        } else if (tokens.access_token === null) {
            console.log("Provided service account does not have permission to generate access tokens");
        } else {
            console.log("pt 2");
            accessToken = tokens.access_token;
            dowork(callback);
        }
    });



    // Generating a new auth token
    async function dowork(callback) {
        console.log(accessToken);
        let baseurl = 'https://zohoio.firebaseio.com/hoopla/';
        let patchurl = 'https://zohoio.firebaseio.com/hoopla/users/' + item.fName + item.lName + '.json?access_token=' + accessToken;
        let policypatchurl = 'https://zohoio.firebaseio.com/hoopla/users/' + item.fName + item.lName + '/policies/' + d.getMonth() + d.getFullYear() + '/' + item.id + '.json?access_token=' + accessToken;
        let carrierurl = 'https://zohoio.firebaseio.com/hoopla/carriers/' + item.carrier.replace(/\s|\./g, "-") + '/policies/' + d.getMonth() + d.getFullYear() + '/' + item.id + '.json?access_token=' + accessToken;
        let basecarrierurl = 'https://zohoio.firebaseio.com/hoopla/carriers/' + item.carrier.replace(/\s|\./g, "-") + '/policies/' + d.getMonth() + d.getFullYear() + '.json?access_token=' + accessToken;
        let basepolicyurl = baseurl + 'users/' + item.fName + item.lName + '/policies/' + d.getMonth() + d.getFullYear() + '.json?access_token=' + accessToken;

        if (item.modifiedDate.substring(0, 16) !== item.createdDate.substring(0, 16)) {
            await axios.get(carrierurl).then((result) => {
                let oguser = result.data.owner.replace(/\s/g, '');
                console.log("replacing data");
                return axios.delete('https://zohoio.firebaseio.com/hoopla/users/' + oguser + '/policies/' + d.getMonth() + d.getFullYear() + '/' + item.id + '.json?access_token=' + accessToken)
                    .catch(err => console.log(err));
            }).catch(err => console.log(err));
        }


        await axios.patch(patchurl, init).then(() => {
            return axios.patch(policypatchurl, policyinfo).then(() => {
                return axios.patch(carrierurl, policyinfo).then(() => {
                    //return axios.get(carrierurl).then((result) => {
                    //     let sum = result.data;
                    //console.log('all carrier objects are', sum);
                    // }).catch(() => {
                    //     console.log(error);
                    // });
                }).catch(error => {
                });
            }).catch(error => {
                console.log(error);
            });
        }).catch(error => {
            console.log(error);
        });

        let sum;
        let carriersum;
        //------sums up the premiums under a carrier or user-----//

        await axios.get(basepolicyurl).then((result) => {
            //console.log(result);
            sum = result.data;
            console.log(typeof sum);
            // console.log('all policies from user is',sum);

            //console.log(typeof sum);

            //sum=JSON.parse(sum);

            let sumarray = [];

            for (key in sum) {
                sumarray.push(Object.assign(sum[key]));
            }
            sum = sumarray;

            //console.log(sum);

            sum = sum.map((item) => {
                if (item.customerPolicyType) {
                    if (item.customerPolicyType.replace(/\s/g, "") != "NewBusiness") {
                        return 0;
                    }
                }

                if (isNaN(item.premium)) {
                    console.log("user premium is NaN");
                    return 0;
                }
                return parseFloat(item.premium);
            });
            sum = sum.reduce(add, 0);
            console.log(sum);


        });

        function add(total, num) {

            return total + num;
        }

        //console.log(idea);
        await axios.get(basecarrierurl).then((result) => {
            carriersum = result.data;
            temparray = [];
            for (key in carriersum) {

                if (carriersum[key].customerPolicyType) {
                    if (carriersum[key].customerPolicyType == "ReWrite")
                        carriersum[key].premium = 0;
                }

                if (isNaN(carriersum[key].premium)) {
                    carriersum[key].premium = 0;
                }


                temparray.push(parseFloat(carriersum[key].premium));
            };
            carriersum = temparray.reduce(add, 0);
            console.log('carrier sum is', carriersum);



        }).catch(err => console.log(err));

        if (sum && carriersum && item.email && item.carrier) {
            await hoopla(sum.toFixed(2), carriersum.toFixed(2), item.email, item.carrier);
        }
        else {
            console.log("could not insert into hoopla, info missing: ", item);
            callback(null, {
                statuscode: 111,
                body: JSON.stringify({
                    code: accessToken,
                    body: item,
                    sumis: sum,
                    carriersum: carriersum
                })
            });
        }


        //await axios.get()

        console.log("finished");

        callback(null, {
            statuscode: 200,
            body: JSON.stringify({
                code: accessToken,
                body: item,
                sumis: sum,
                carriersum: carriersum
            })
        });

    }



};
