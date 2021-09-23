use futures::executor::LocalPool;
use near_crypto::InMemorySigner;
use near_primitives::{borsh::BorshSerialize, types::FunctionArgs};
use runner;
use serde_json::Value;

pub fn call(
    signer: &InMemorySigner,
    account_id: &str,
    contract_id: &str,
    method: &str,
    value: Option<Value>,
) {
    let mut pool = LocalPool::new();
    pool.run_until(async {
        runner::call(
            signer,
            account_id.to_owned(),
            contract_id.to_owned(),
            method.to_owned(),
            value
                .unwrap_or_default()
                .to_string()
                .into_bytes(),
            None,
        )
        .await
        .unwrap()
    });
}

pub fn view(contract_id: &str, method: &str, value: Option<Value>) -> serde_json::Value {
    let mut pool = LocalPool::new();
    pool.run_until(async {
        runner::view(
            contract_id.to_owned(),
            method.to_owned(),
            FunctionArgs::from(
                value
                    .unwrap_or_default()
                    .to_string()
                    .into_bytes(),
            ),
        )
        .await
        .unwrap()
    })
}

#[macro_export]
macro_rules! invoke_call {
    ($self: ident, $context: ident, $method: tt) => {
        crate::actions::call(
            $context.signer().get(),
            $context.signer().account_id(),
            $context.contracts().get($self.name()).account_id(),
            $method,
            None,
        );
    };
    ($self: ident, $context: ident, $method: tt, $param: ident) => {
        crate::actions::call(
            $context.signer().get(),
            $context.signer().account_id(),
            $context.contracts().get($self.name()).account_id(),
            $method,
            Some($context.$param($method)),
        );
    };
}

#[macro_export]
macro_rules! invoke_view {
    ($self: ident, $context: ident, $method: tt) => {
        let response = crate::actions::view(
            $context.contracts().get($self.name()).account_id(),
            $method,
            None,
        );
        $context.add_method_responses($method, response);
    };
    ($self: ident, $context: ident, $method: tt, $param: ident) => {
        let response = crate::actions::view(
            $context.contracts().get($self.name()).account_id(),
            $method,
            Some($context.$param($method)),
        );
        $context.add_method_responses($method, response);
    };
}
